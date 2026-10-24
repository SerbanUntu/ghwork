package com.ghwork.api;

import com.ghwork.api.exceptions.ApiException;
import com.ghwork.cli.EnvService;
import com.ghwork.cli.exceptions.CliException;
import com.ghwork.domain.AccessToken;
import com.ghwork.domain.Repository;
import com.ghwork.json.JsonService;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client responsible for making requests to the "List jobs for a workflow run" route.
 * Instantiated via {@link JobClientFactory}.
 */
public class JobClient extends GithubClient {

    private static final Logger LOG = LogManager.getLogger(JobClient.class);
    private final JsonService jsonService;

    /**
     * Generates the subroute given a workflow run id.
     *
     * @param runId The id of the workflow run.
     * @return The generated subroute, to be appended to the base route provided by
     *         {@link GithubClient}.
     */
    private static String getRoute(long runId) {
        return "/actions/runs/" + runId + "/jobs";
    }

    /**
     * Constructor for a client that fetches information about GitHub Actions jobs. Meant to be
     * called by {@link JobClientFactory}.
     *
     * @param envService The service handling environment variables.
     * @param httpClient The HTTP client used to make requests.
     * @param jsonService The service handling JSON (de)serialization.
     * @param repository The data object containing the GitHub repository data.
     * @param accessToken The user's personal access token.
     * @param apiBaseRoute The protocol and domain of the URL.
     */
    JobClient(EnvService envService, HttpClient httpClient, JsonService jsonService,
        Repository repository, AccessToken accessToken, String apiBaseRoute) {
        super(envService, httpClient, repository, accessToken, apiBaseRoute);
        this.jsonService = jsonService;
    }

    /**
     * Makes multiple parallel requests to the jobs route and returns the parsed data.
     *
     * @param runIds The ids of the workflow runs.
     * @return The responses from the API, parsed as value objects.
     * @throws ApiException If a network fault occurs.
     * @throws CliException If the repository name or access token provided by the user are invalid.
     */
    public List<JobResponse> fetchData(List<Long> runIds) throws ApiException, CliException {
        if (runIds == null || runIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<URI> uris = runIds.stream().map(runId -> {
            try {
                return new URI(baseRoute + getRoute(runId));
            } catch (URISyntaxException e) {
                throw new ApiException(e.getMessage());
            }
        }).toList();

        List<CompletableFuture<HttpResponse<String>>> futures = uris
            .stream()
            .map(uri -> {
                if (this.envService.isDebugPrintingEnabled()) {
                    LOG.debug("GET request: {}", uri);
                }
                return HttpRequest.newBuilder().uri(uri).headers(headers)
                    .timeout(envService.getRequestTimeout()).build();
            })
            .map(request -> CompletableFuture.supplyAsync(() -> getResponse(request)))
            .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
        } catch (CompletionException e) {
            // Rethrow the original exception to report it as well
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new ApiException(e.getMessage(), e);
        }

        return futures
            .stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ApiException(e.getMessage());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    throw new ApiException(cause == null ? e.getMessage() : cause.getMessage());
                }
            })
            .filter(Objects::nonNull)
            .map(HttpResponse::body)
            .filter(Objects::nonNull)
            .map(jsonService::parseJobResponse)
            .toList();
    }
}
