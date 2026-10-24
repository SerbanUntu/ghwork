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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client responsible for making requests to the "List workflow runs for a repository" route.
 * Instantiated via {@link WorkflowClientFactory}.
 */
public class WorkflowClient extends GithubClient {

    private static final String route = "/actions/runs";
    private static final Logger LOG = LogManager.getLogger(WorkflowClient.class);

    private final HttpRequest request;
    private final JsonService jsonService;

    /**
     * Constructor for a client that fetches information about GitHub Actions workflows.
     * Meant to be called by {@link WorkflowClientFactory}.
     *
     * @param envService   The service handling environment variables.
     * @param httpClient   The HTTP client used to make requests.
     * @param jsonService  The service handling JSON (de)serialization.
     * @param repository   The data object containing the GitHub repository data.
     * @param accessToken  The user's personal access token.
     * @param apiBaseRoute The protocol and domain of the URL.
     */
    WorkflowClient(
        EnvService envService,
        HttpClient httpClient,
        JsonService jsonService,
        Repository repository,
        AccessToken accessToken,
        String apiBaseRoute
    ) {
        super(envService, httpClient, repository, accessToken, apiBaseRoute);
        this.jsonService = jsonService;
        try {
            this.request = HttpRequest.newBuilder()
                .uri(new URI(baseRoute + route))
                .headers(headers)
                .timeout(envService.getRequestTimeout())
                .build();
        } catch (URISyntaxException e) {
            throw new ApiException(e.getMessage());
        }
    }

    /**
     * Makes a request to the workflows route and returns the parsed data.
     *
     * @return The response from the API, parsed as a value object.
     * @throws ApiException If a network fault occurs.
     * @throws CliException If the repository name or access token provided by the user are invalid.
     */
    public WorkflowResponse fetchData() throws ApiException, CliException {
        if (this.envService.isDebugPrintingEnabled()) {
            LOG.debug("GET request: {}", this.request.uri());
        }
        var response = getResponse(this.request);
        return jsonService.parseWorkflowResponse(response.body());
    }
}
