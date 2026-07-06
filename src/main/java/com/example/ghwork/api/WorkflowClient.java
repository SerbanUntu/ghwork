package com.example.ghwork.api;

import com.example.ghwork.api.exceptions.ApiException;
import com.example.ghwork.cli.exceptions.CliException;
import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;
import com.example.ghwork.json.JsonService;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

/**
 * The client responsible for making requests to the "List workflow runs for a repository" route.
 */
public class WorkflowClient extends GithubClient {

    private static final String route = "/actions/runs";

    private final HttpRequest request;
    private final JsonService jsonService = new JsonService();

    /**
     * Constructor for a client that fetches information about GitHub Actions workflows.
     *
     * @param repository  The repository to fetch workflow data from.
     * @param accessToken The access token needed for accessing the repository.
     * @throws ApiException If URI construction for the API location fails.
     */
    public WorkflowClient(Repository repository, AccessToken accessToken)
        throws ApiException {
        super(repository, accessToken);
        try {
            this.request = HttpRequest.newBuilder()
                .uri(new URI(baseRoute + route))
                .headers(headers)
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
            System.out.println("[DEBUG] GET request: " + this.request.uri());
        }
        var response = getResponse(this.request);
        return jsonService.parseWorkflowResponse(response.body());
    }
}
