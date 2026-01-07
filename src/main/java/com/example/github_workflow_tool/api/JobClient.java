package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.api.exceptions.APIException;
import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.json.JsonService;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
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
     * @return The generated subroute, to be appended to the base route provided by {@link GithubClient}.
     */
    private static String getRoute(long runId) {
        return "/actions/runs/" + runId + "/jobs";
    }

    public JobClient(Repository repository, AccessToken accessToken) {
        super(repository, accessToken);
    }

    /**
     * Makes multiple parallel requests to the jobs route and returns the parsed data.
     *
     * @param runIds The ids of the workflow runs.
     * @return The responses from the API, parsed as value objects.
     * @throws APIException If a network fault occurs.
     * @throws CLIException If the repository name or access token provided by the user are invalid.
     */
    public List<JobResponse> fetchData(List<Long> runIds) throws APIException, CLIException {
        if (runIds == null || runIds.isEmpty()) return new ArrayList<>();
        List<URI> uris = runIds.stream().map(runId -> {
            try {
                return new URI(baseRoute + getRoute(runId));
            } catch (URISyntaxException e) {
                throw new APIException(e.getMessage());
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
                .map(request -> CompletableFuture.supplyAsync(() -> getResponse(request)).exceptionally(e -> {
                    throw new APIException(e.getMessage());
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
                        throw new APIException(e.getMessage());
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        throw new APIException(cause == null ? e.getMessage() : cause.getMessage());
                    }
                })
                .filter(Objects::nonNull)
                .map(HttpResponse::body)
                .filter(Objects::nonNull)
                .map(jsonService::parseJobResponse)
                .toList();
    }
}
