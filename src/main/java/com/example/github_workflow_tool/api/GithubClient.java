package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GithubClient {

    private static final String API_VERSION = "2022-11-28";

    private final HttpClient client;
    private final HttpRequest request;

    public GithubClient(Repository repository, AccessToken token)
            throws UncheckedIOException, URISyntaxException {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" +
                        repository.getRepository() +
                        "/actions/runs"))
                .headers("X-GitHub-Api-Version", API_VERSION, "Authorization", "Bearer " + token.getToken())
                .build();
    }

    public String fetchData() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
