package com.ghwork;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

import com.ghwork.cli.EnvService;
import com.ghwork.cli.runcount.NumberOfTimes;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class GhworkApplicationTests {

    private static final String[] ARGS = {"owner/repo", "token"};
    private static final String RUNS_ROUTE = "/repos/owner/repo/actions/runs";
    private static final String JOBS_ROUTE = RUNS_ROUTE + "/1/jobs";
    private static final String NON_ROUTABLE_ADDRESS = "http://10.255.255.1";

    private static final int MAX_FAILED_POLLS = new EnvService().getMaxRetryAttempts() + 1;

    private static final String NO_RUNS = """
        {"total_count": 0, "workflow_runs": []}
        """;

    private static final String ONE_RUN = """
        {
          "total_count": 1,
          "workflow_runs": [
            {
              "id": 1,
              "workflow_id": 2,
              "head_sha": "123abc",
              "name": "ci",
              "head_branch": "main",
              "status": "in_progress",
              "conclusion": null,
              "created_at": "2026-01-01T19:00:00Z",
              "updated_at": "2026-01-01T19:00:00Z",
              "run_started_at": "2026-01-01T19:00:00Z"
            }
          ]
        }
        """;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort().dynamicHttpsPort()).build();

    @TempDir
    private Path tempDir;

    private ListAppender logs;

    @BeforeEach
    public void setUp() {
        logs = ListAppender.getListAppender("ListAppender");
        logs.clear();
    }

    private int runApplication(String baseUrl, int polls) {
        return GhworkApplication.runApplication(ARGS, new PrintStream(new ByteArrayOutputStream()),
            baseUrl, new NumberOfTimes(polls),
            Optional.of(tempDir.resolve("ghwork-test-data.ser")));
    }

    private int runApplication(int polls) {
        return runApplication(wm.getRuntimeInfo().getHttpBaseUrl(), polls);
    }

    private long countLogs(Level level) {
        return logs.getEvents().stream().filter(event -> event.getLevel() == level).count();
    }

    private String joinLogs(Level level) {
        return String.join("\n",
            logs.getEvents().stream().filter(event -> event.getLevel() == level)
                .map(event -> event.getMessage().getFormattedMessage()).toList());
    }

    private void assertGaveUpAfterRetrying(int exitCode, String expectedCause) {
        assertEquals(1, exitCode);
        assertEquals(MAX_FAILED_POLLS, countLogs(Level.WARN));
        assertTrue(joinLogs(Level.WARN).contains(expectedCause), joinLogs(Level.WARN));
        assertTrue(joinLogs(Level.ERROR).contains(MAX_FAILED_POLLS + " times in a row"));
    }

    private void assertFailedWithoutRetrying(int exitCode, String expectedCause) {
        assertEquals(1, exitCode);
        assertEquals(0, countLogs(Level.WARN), joinLogs(Level.WARN));
        assertTrue(joinLogs(Level.ERROR).contains(expectedCause), joinLogs(Level.ERROR));
    }

    @Test
    public void successfulPollsTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(NO_RUNS)));

        assertEquals(0, runApplication(2));
        wm.verify(2, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
        assertEquals(0, countLogs(Level.WARN));
    }

    @ParameterizedTest
    @EnumSource(value = Fault.class, names = {"CONNECTION_RESET_BY_PEER", "EMPTY_RESPONSE",
        "MALFORMED_RESPONSE_CHUNK"})
    public void retriesOnFaultTest(Fault fault) {
        wm.stubFor(get(RUNS_ROUTE).willReturn(aResponse().withFault(fault)));

        assertGaveUpAfterRetrying(runApplication(1), "Exception: ");
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 429, 500, 502, 503, 504})
    public void retriesOnStatusCodeTest(int code) {
        wm.stubFor(get(RUNS_ROUTE).willReturn(aResponse().withStatus(code)));

        assertGaveUpAfterRetrying(runApplication(1), "Status code " + code);
        wm.verify(MAX_FAILED_POLLS, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 410, 422, 501, 505})
    public void doesNotRetryOnStatusCodeTest(int code) {
        wm.stubFor(get(RUNS_ROUTE).willReturn(aResponse().withStatus(code)));

        assertFailedWithoutRetrying(runApplication(1), "Status code " + code);
        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
    }

    @Test
    public void doesNotRetryOnUnauthorizedTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(unauthorized()));

        assertFailedWithoutRetrying(runApplication(1), "not a valid GitHub personal access token");
        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
    }

    @Test
    public void doesNotRetryOnNotFoundTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(notFound()));

        assertFailedWithoutRetrying(runApplication(1), "does not exist");
        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
    }

    @Test
    public void retriesOnRequestTimeoutTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(NO_RUNS).withFixedDelay(2000)));

        assertGaveUpAfterRetrying(runApplication(1), "HttpTimeoutException");
    }

    @Test
    public void retriesOnConnectTimeoutTest() {
        assertGaveUpAfterRetrying(runApplication(NON_ROUTABLE_ADDRESS, 1),
            "HttpConnectTimeoutException");
    }

    @Test
    public void retriesOnConnectionRefusedTest() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }

        assertGaveUpAfterRetrying(runApplication("http://localhost:" + closedPort, 1),
            "ConnectException");
    }

    @Test
    public void retriesOnUnknownHostTest() {
        assertGaveUpAfterRetrying(runApplication("http://invalid.invalid", 1), "ConnectException");
    }

    @Test
    public void doesNotRetryOnUntrustedCertificateTest() {
        // WireMock's HTTPS port uses a self-signed certificate
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(NO_RUNS)));

        assertEquals(1, runApplication(wm.getRuntimeInfo().getHttpsBaseUrl(), 1));
        assertTrue(joinLogs(Level.ERROR).contains("PKIX path building failed"));
        assertFalse(joinLogs(Level.WARN).contains("PKIX"), joinLogs(Level.WARN));
        wm.verify(0, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
    }

    @Test
    public void retriesOnJobsRouteErrorTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(ONE_RUN)));
        wm.stubFor(get(JOBS_ROUTE).willReturn(serviceUnavailable()));

        assertGaveUpAfterRetrying(runApplication(1), "Status code 503");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not json", "{\"total_count\": 1, \"workflow_runs\": [", "{}"})
    public void retriesOnInvalidJsonRunsResponseTest(String body) {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(body)));

        assertGaveUpAfterRetrying(runApplication(1), "could not be parsed as WorkflowResponse");
    }

    @Test
    public void retriesOnInvalidJsonJobsResponseTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(ONE_RUN)));
        // noinspection JsonStandardCompliance
        wm.stubFor(get(JOBS_ROUTE).willReturn(okJson("<html>502 Bad Gateway</html>")));

        assertGaveUpAfterRetrying(runApplication(1), "could not be parsed as JobResponse");
    }

    @Test
    public void doesNotRetryOnJobsRouteUnauthorizedTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(ONE_RUN)));
        wm.stubFor(get(JOBS_ROUTE).willReturn(unauthorized()));

        assertFailedWithoutRetrying(runApplication(1), "not a valid GitHub personal access token");
    }

    @Test
    public void recoversAfterRetriableErrorTest() {
        wm.stubFor(get(RUNS_ROUTE).inScenario("recovery").whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serviceUnavailable()).willSetStateTo("recovered"));
        wm.stubFor(get(RUNS_ROUTE).inScenario("recovery").whenScenarioStateIs("recovered")
            .willReturn(okJson(NO_RUNS)));

        assertEquals(0, runApplication(1));
        assertEquals(1, countLogs(Level.WARN));
    }

    @Test
    public void resetsFailureCountAfterSuccessfulPollTest() {
        wm.stubFor(get(RUNS_ROUTE).inScenario("alternating").whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serviceUnavailable()).willSetStateTo("succeeding"));
        wm.stubFor(get(RUNS_ROUTE).inScenario("alternating").whenScenarioStateIs("succeeding")
            .willReturn(okJson(NO_RUNS)).willSetStateTo(Scenario.STARTED));

        assertEquals(0, runApplication(MAX_FAILED_POLLS + 1));
        assertEquals(MAX_FAILED_POLLS + 1, countLogs(Level.WARN));
    }
}
