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
 * The client responsible for making requests to the "List jobs for a workflow run" route.
 */
public class JobClient extends GithubClient {

    private final JsonService jsonService = new JsonService();

    /**
     * Generates the subroute given a workflow run id.
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
     * Makes a request to the jobs route and returns the parsed data.
     * @param runId The id of the workflow run.
     * @return The response from the API, parsed as a value object.
     * @throws APIException If a network fault occurs.
     * @throws CLIException If the repository name or access token provided by the user are invalid.
     */
    public JobResponse fetchData(long runId) throws APIException, CLIException {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(baseRoute + getRoute(runId)))
                    .headers(headers)
                    .build();
        } catch (URISyntaxException e) {
            throw new APIException(e.getMessage());
        }
        var response = getResponse(request);
        return jsonService.parseJobResponse(response.body());
    }
}
