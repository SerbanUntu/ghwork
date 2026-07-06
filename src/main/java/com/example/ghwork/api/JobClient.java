package com.example.ghwork.api;

import com.example.ghwork.api.exceptions.ApiException;
import com.example.ghwork.cli.exceptions.CliException;
import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;
import com.example.ghwork.json.JsonService;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * The client responsible for making requests to the "List jobs for a workflow run" route.
 */
public class JobClient extends GithubClient {

    private final JsonService jsonService = new JsonService();

    /**
     * Generates the subroute given a workflow run id.
     *
     * @param runId The id of the workflow run.
     * @return The generated subroute, to be appended to the base route
     *     provided by {@link GithubClient}.
     */
    private static String getRoute(long runId) {
        return "/actions/runs/" + runId + "/jobs";
    }

    /**
     * Constructor for a client that fetches information about GitHub Actions jobs.
     *
     * @param repository  The repository to fetch job data from.
     * @param accessToken The access token needed for accessing the repository.
     */
    public JobClient(Repository repository, AccessToken accessToken) {
        super(repository, accessToken);
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
                    System.out.println("[DEBUG] GET request: " + uri);
                }
                return HttpRequest.newBuilder().uri(uri).headers(headers).build();
            })
            .map(request -> CompletableFuture.supplyAsync(() -> getResponse(request))
                .exceptionally(e -> {
                    throw new ApiException(e.getMessage());
                }))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

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
