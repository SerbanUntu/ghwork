package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.json.JsonService;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WorkflowClient extends GithubClient {

    private static final String route = "/actions/runs";

    private final HttpRequest request;

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

    public WorkflowResponse fetchData() throws APIException, CLIException {
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new APIException(e.getMessage());
        }
        checkResponse(response);
        return (new JsonService()).parseWorkflowResponse(response.body());
    }
}
