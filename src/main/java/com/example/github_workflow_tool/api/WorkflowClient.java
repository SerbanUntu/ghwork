package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.api.exceptions.APIException;
import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.json.JsonService;

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

    public WorkflowClient(Repository repository, AccessToken accessToken)
            throws APIException {
        super(repository, accessToken);
        try {
            this.request = HttpRequest.newBuilder()
                    .uri(new URI(baseRoute + route))
                    .headers(headers)
                    .build();
        } catch (URISyntaxException e) {
            throw new APIException(e.getMessage());
        }
    }

    /**
     * Makes a request to the workflows route and returns the parsed data.
     * @return The response from the API, parsed as a value object.
     * @throws APIException If a network fault occurs.
     * @throws CLIException If the repository name or access token provided by the user are invalid.
     */
    public WorkflowResponse fetchData() throws APIException, CLIException {
        if (this.envService.isDebugPrintingEnabled()) {
            System.out.println("[DEBUG] GET request: " + this.request.uri());
        }
        var response = getResponse(this.request);
        return jsonService.parseWorkflowResponse(response.body());
    }
}
