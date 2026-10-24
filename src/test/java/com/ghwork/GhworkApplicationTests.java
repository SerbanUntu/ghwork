package com.ghwork;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

import com.ghwork.cli.CliPrinter;
import com.ghwork.cli.EnvService;
import com.ghwork.cli.StorageService;
import com.ghwork.cli.runcount.NumberOfTimes;
import com.ghwork.domain.Job;
import com.ghwork.domain.JobStep;
import com.ghwork.domain.Repository;
import com.ghwork.domain.ToolState;
import com.ghwork.domain.WorkflowRun;
import com.ghwork.domain.WorkflowRunData;
import com.ghwork.domain.events.Event;
import com.ghwork.domain.events.JobFinishedEvent;
import com.ghwork.domain.events.JobStartedEvent;
import com.ghwork.domain.events.StepFailedEvent;
import com.ghwork.domain.events.StepStartedEvent;
import com.ghwork.domain.events.StepSucceededEvent;
import com.ghwork.domain.events.WorkflowQueuedEvent;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private static final String JSON_NO_RUNS = """
        {"total_count": 0, "workflow_runs": []}
        """;

    private static final String JSON_ONE_RUN = """
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

    private static final Map<Long, String> E2E_SHA = Map.of(
        1L, "1111111aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        2L, "2222222bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
        3L, "3333333ccccccccccccccccccccccccccccccccc",
        99L, "9999999fffffffffffffffffffffffffffffffff");

    private static final String JSON_E2E_THREE_RUNS = """
        {
          "total_count": 3,
          "workflow_runs": [
            {
              "id": 1,
              "workflow_id": 100,
              "head_sha": "%1$s",
              "name": "ci",
              "head_branch": "main",
              "status": "in_progress",
              "conclusion": null,
              "created_at": "2026-01-01T19:00:00Z",
              "updated_at": "2026-01-01T19:03:00Z",
              "run_started_at": "2026-01-01T19:00:00Z"
            },
            {
              "id": 2,
              "workflow_id": 200,
              "head_sha": "%2$s",
              "name": "deploy",
              "head_branch": "feature",
              "status": "in_progress",
              "conclusion": null,
              "created_at": "2026-01-01T19:04:00Z",
              "updated_at": "2026-01-01T19:05:00Z",
              "run_started_at": "2026-01-01T19:05:00Z"
            },
            {
              "id": 99,
              "workflow_id": 100,
              "head_sha": "%3$s",
              "name": "ci",
              "head_branch": "main",
              "status": "completed",
              "conclusion": "success",
              "created_at": "2026-01-01T18:00:00Z",
              "updated_at": "2026-01-01T18:10:00Z",
              "run_started_at": "2026-01-01T18:00:00Z"
            }
          ]
        }
        """.formatted(E2E_SHA.get(1L), E2E_SHA.get(2L), E2E_SHA.get(99L));

    private static final String JSON_E2E_RUN_1_JOBS = """
        {
          "total_count": 1,
          "jobs": [
            {
              "id": 10,
              "run_id": 1,
              "head_sha": "%s",
              "status": "completed",
              "conclusion": "success",
              "name": "build",
              "started_at": "2026-01-01T19:00:00Z",
              "completed_at": "2026-01-01T19:03:00Z",
              "steps": [
                {
                  "name": "Set up job",
                  "status": "completed",
                  "conclusion": "success",
                  "number": 1,
                  "started_at": "2026-01-01T19:00:00Z",
                  "completed_at": "2026-01-01T19:01:00Z"
                },
                {
                  "name": "Build",
                  "status": "completed",
                  "conclusion": "success",
                  "number": 2,
                  "started_at": "2026-01-01T19:01:00Z",
                  "completed_at": "2026-01-01T19:03:00Z"
                }
              ]
            }
          ]
        }
        """.formatted(E2E_SHA.get(1L));

    private static final String JSON_E2E_RUN_2_JOBS = """
        {
          "total_count": 1,
          "jobs": [
            {
              "id": 20,
              "run_id": 2,
              "head_sha": "%s",
              "status": "in_progress",
              "conclusion": null,
              "name": "deploy",
              "started_at": "2026-01-01T19:05:00Z",
              "completed_at": null,
              "steps": [
                {
                  "name": "Set up job",
                  "status": "in_progress",
                  "conclusion": null,
                  "number": 1,
                  "started_at": "2026-01-01T19:05:00Z",
                  "completed_at": null
                }
              ]
            }
          ]
        }
        """.formatted(E2E_SHA.get(2L));

    private static final String JSON_E2E_RUN_3_JOBS = """
        {
          "total_count": 1,
          "jobs": [
            {
              "id": 30,
              "run_id": 3,
              "head_sha": "%s",
              "status": "completed",
              "conclusion": "failure",
              "name": "lint",
              "started_at": "2026-01-01T18:50:00Z",
              "completed_at": "2026-01-01T19:02:00Z",
              "steps": [
                {
                  "name": "Lint",
                  "status": "completed",
                  "conclusion": "failure",
                  "number": 1,
                  "started_at": "2026-01-01T18:50:00Z",
                  "completed_at": "2026-01-01T19:02:00Z"
                }
              ]
            }
          ]
        }
        """.formatted(E2E_SHA.get(3L));

    private static Instant at(int hour, int minute) {
        return Instant.parse("2026-01-01T%02d:%02d:00Z".formatted(hour, minute));
    }

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

    private Path filePath() {
        return tempDir.resolve("ghwork-test-data.ser");
    }

    private int runApplication(String baseUrl, int polls, PrintStream out) {
        return GhworkApplication.runApplication(ARGS, out, baseUrl, new NumberOfTimes(polls),
            Optional.of(filePath()));
    }

    private int runApplication(String baseUrl, int polls) {
        return runApplication(baseUrl, polls, new PrintStream(new ByteArrayOutputStream()));
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
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_NO_RUNS)));

        assertEquals(0, runApplication(2));
        wm.verify(2, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
        assertEquals(0, countLogs(Level.WARN));
    }

    @Test
    public void pollsOnceWithSeededCacheTest() {
        WorkflowRun run1Before = new WorkflowRun(1, 100, E2E_SHA.get(1L), "ci", "main",
            "in_progress", null, at(19, 0), at(19, 1), at(19, 0));
        WorkflowRun run1After = new WorkflowRun(1, 100, E2E_SHA.get(1L), "ci", "main",
            "in_progress", null, at(19, 0), at(19, 3), at(19, 0));
        WorkflowRun run2 = new WorkflowRun(2, 200, E2E_SHA.get(2L), "deploy", "feature",
            "in_progress", null, at(19, 4), at(19, 5), at(19, 5));
        WorkflowRun run3 = new WorkflowRun(3, 300, E2E_SHA.get(3L), "lint", "main",
            "in_progress", null, at(18, 50), at(18, 50), at(18, 50));
        WorkflowRun run99 = new WorkflowRun(99, 100, E2E_SHA.get(99L), "ci", "main",
            "completed", "success", at(18, 0), at(18, 10), at(18, 0));

        JobStep setUpStep = new JobStep("Set up job", "completed", "success", 1, at(19, 0),
            at(19, 1));
        Job job10Before = new Job(10, 1, E2E_SHA.get(1L), "in_progress", null, "build",
            at(19, 0), null,
            List.of(setUpStep, new JobStep("Build", "in_progress", null, 2, at(19, 1), null)));
        Job job10After = new Job(10, 1, E2E_SHA.get(1L), "completed", "success", "build",
            at(19, 0), at(19, 3), List.of(setUpStep,
            new JobStep("Build", "completed", "success", 2, at(19, 1), at(19, 3))));
        Job job20 = new Job(20, 2, E2E_SHA.get(2L), "in_progress", null, "deploy", at(19, 5),
            null, List.of(new JobStep("Set up job", "in_progress", null, 1, at(19, 5), null)));
        Job job30Before = new Job(30, 3, E2E_SHA.get(3L), "in_progress", null, "lint",
            at(18, 50), null,
            List.of(new JobStep("Lint", "in_progress", null, 1, at(18, 50), null)));

        Repository repository = new Repository("owner/repo");
        Repository otherRepository = new Repository("other/repo");
        ToolState otherToolState = new ToolState(Map.of(), Set.of(6L));
        new StorageService(new EnvService(), filePath()).save(new HashMap<>(Map.of(
            repository, new ToolState(Map.of(
                1L, new WorkflowRunData(run1Before, Map.of(10L, job10Before)),
                3L, new WorkflowRunData(run3, Map.of(30L, job30Before)),
                99L, new WorkflowRunData(run99, Map.of())), Set.of(99L)),
            otherRepository, otherToolState)));

        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_E2E_THREE_RUNS)));
        wm.stubFor(get(RUNS_ROUTE + "/1/jobs").willReturn(okJson(JSON_E2E_RUN_1_JOBS)));
        wm.stubFor(get(RUNS_ROUTE + "/2/jobs").willReturn(okJson(JSON_E2E_RUN_2_JOBS)));
        wm.stubFor(get(RUNS_ROUTE + "/3/jobs").willReturn(okJson(JSON_E2E_RUN_3_JOBS)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(0, runApplication(wm.getRuntimeInfo().getHttpBaseUrl(), 1,
            new PrintStream(out)));

        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE + "/1/jobs")));
        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE + "/2/jobs")));
        wm.verify(1, getRequestedFor(urlEqualTo(RUNS_ROUTE + "/3/jobs")));
        wm.verify(0, getRequestedFor(urlEqualTo(RUNS_ROUTE + "/99/jobs")));

        List<Event> expectedEvents = List.of(
            new StepFailedEvent(at(19, 2), "main", E2E_SHA.get(3L), 3, 30, "Lint", 1, "lint"),
            new JobFinishedEvent(at(19, 2), "main", E2E_SHA.get(3L), 3, 30, "lint", "lint"),
            new StepSucceededEvent(at(19, 3), "main", E2E_SHA.get(1L), 1, 10, "Build", 2, "build"),
            new JobFinishedEvent(at(19, 3), "main", E2E_SHA.get(1L), 1, 10, "build", "ci"),
            new WorkflowQueuedEvent(at(19, 5), "feature", E2E_SHA.get(2L), 2, "deploy"),
            new JobStartedEvent(at(19, 5), "feature", E2E_SHA.get(2L), 2, 20, "deploy", "deploy"),
            new StepStartedEvent(at(19, 5), "feature", E2E_SHA.get(2L), 2, 20, "Set up job", 1,
                "deploy"));
        assertEquals(new CliPrinter().prettyPrintOnSeparateLines(expectedEvents)
            + System.lineSeparator(), out.toString());

        Map<Repository, ToolState> expectedCache = Map.of(
            repository, new ToolState(Map.of(
                1L, new WorkflowRunData(run1After, Map.of(10L, job10After)),
                2L, new WorkflowRunData(run2, Map.of(20L, job20)),
                99L, new WorkflowRunData(run99, Map.of())), Set.of(99L)),
            otherRepository, otherToolState);
        assertEquals(expectedCache, new StorageService(new EnvService(), filePath()).retrieve());

        assertEquals(0, countLogs(Level.WARN), joinLogs(Level.WARN));
        assertEquals(0, countLogs(Level.ERROR), joinLogs(Level.ERROR));
        assertFalse(joinLogs(Level.INFO).contains("No data found"), joinLogs(Level.INFO));
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
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_NO_RUNS).withFixedDelay(2000)));

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
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_NO_RUNS)));

        assertEquals(1, runApplication(wm.getRuntimeInfo().getHttpsBaseUrl(), 1));
        assertTrue(joinLogs(Level.ERROR).contains("PKIX path building failed"));
        assertFalse(joinLogs(Level.WARN).contains("PKIX"), joinLogs(Level.WARN));
        wm.verify(0, getRequestedFor(urlEqualTo(RUNS_ROUTE)));
    }

    @Test
    public void retriesOnJobsRouteErrorTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_ONE_RUN)));
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
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_ONE_RUN)));
        // noinspection JsonStandardCompliance
        wm.stubFor(get(JOBS_ROUTE).willReturn(okJson("<html>502 Bad Gateway</html>")));

        assertGaveUpAfterRetrying(runApplication(1), "could not be parsed as JobResponse");
    }

    @Test
    public void doesNotRetryOnJobsRouteUnauthorizedTest() {
        wm.stubFor(get(RUNS_ROUTE).willReturn(okJson(JSON_ONE_RUN)));
        wm.stubFor(get(JOBS_ROUTE).willReturn(unauthorized()));

        assertFailedWithoutRetrying(runApplication(1), "not a valid GitHub personal access token");
    }

    @Test
    public void recoversAfterRetriableErrorTest() {
        wm.stubFor(get(RUNS_ROUTE).inScenario("recovery").whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serviceUnavailable()).willSetStateTo("recovered"));
        wm.stubFor(get(RUNS_ROUTE).inScenario("recovery").whenScenarioStateIs("recovered")
            .willReturn(okJson(JSON_NO_RUNS)));

        assertEquals(0, runApplication(1));
        assertEquals(1, countLogs(Level.WARN));
    }

    @Test
    public void resetsFailureCountAfterSuccessfulPollTest() {
        wm.stubFor(get(RUNS_ROUTE).inScenario("alternating").whenScenarioStateIs(Scenario.STARTED)
            .willReturn(serviceUnavailable()).willSetStateTo("succeeding"));
        wm.stubFor(get(RUNS_ROUTE).inScenario("alternating").whenScenarioStateIs("succeeding")
            .willReturn(okJson(JSON_NO_RUNS)).willSetStateTo(Scenario.STARTED));

        assertEquals(0, runApplication(MAX_FAILED_POLLS + 1));
        assertEquals(MAX_FAILED_POLLS + 1, countLogs(Level.WARN));
    }
}
