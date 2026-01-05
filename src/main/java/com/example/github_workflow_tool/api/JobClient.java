package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.json.JsonService;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JobClient extends GithubClient {

    private static String getRoute(long runId) {
        return "/actions/runs/" + runId + "/jobs";
    }

    public JobClient(Repository repository, AccessToken accessToken) {
        super(repository, accessToken);
    }

    public JobResponse fetchData(long runId) throws APIException, CLIException {
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(baseRoute + getRoute(runId)))
                    .headers(headers)
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new APIException(e.getMessage());
        }
        checkResponse(response);
        return (new JsonService()).parseJobResponse(response.body());
    }
}
