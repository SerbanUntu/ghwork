package com.example.github_workflow_tool.api;

import com.example.github_workflow_tool.api.exceptions.APIException;
import com.example.github_workflow_tool.cli.EnvService;
import com.example.github_workflow_tool.cli.exceptions.GenericClientError;
import com.example.github_workflow_tool.api.exceptions.ServerError;
import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.cli.exceptions.InexistentRepositoryException;
import com.example.github_workflow_tool.cli.exceptions.InvalidAccessTokenException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Abstracts the interactions with the GitHub's REST API.
 */
public abstract class GithubClient {

    private static final String API_VERSION = "2022-11-28";

    protected final Repository repository;
    protected final HttpClient client;
    protected final EnvService envService;

    /**
     * The route shared by all requests. Contains the base URL, the owner, and the repository name.
     */
    protected final String baseRoute;

    /**
     * The headers needed when sending requests to the GitHub API, in the form: header, value, header, value.
     */
    protected final String[] headers;

    /**
     * Throws a CLI exception if the API returns a 4xx status code due to incorrect parameters passed to the CLI.
     *
     * @param response The HTTP Response object.
     * @throws CLIException If the repository is not found or the access token is invalid.
     */
    private void checkResponse(HttpResponse<String> response) throws APIException, CLIException {
        int code = response.statusCode();
        switch (code) {
            case 401:
                throw new InvalidAccessTokenException();
            case 404:
                throw new InexistentRepositoryException(repository);
        }
        if (code >= 400 && code <= 499) throw new GenericClientError(code);
        if (code >= 500 && code <= 599) throw new ServerError(code);
    }

    /**
     * Wrapper around a normal `client.send` call that turns technical errors into user-facing errors
     * and check the returned status code.
     *
     * @param request The request object to send to the GitHub API.
     * @return A valid response from the GitHub server.
     * @throws APIException If a network fault occurs.
     * @throws CLIException If the repository name or access token provided by the user are invalid.
     */
    protected HttpResponse<String> getResponse(HttpRequest request) throws APIException, CLIException {
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new APIException(e.getMessage());
        } catch (IOException e) {
            throw new APIException(e.getMessage());
        }
        checkResponse(response);
        return response;
    }

    /**
     * Initializes the HTTP client and request data.
     *
     * @param repository The data object containing the GitHub repository data.
     * @param token      The user's personal access token.
     */
    public GithubClient(Repository repository, AccessToken token) {
        this.repository = repository;
        this.client = HttpClient.newHttpClient();
        this.baseRoute = "https://api.github.com/repos/" + repository.getOwner() + "/" + repository.getRepository();
        this.envService = new EnvService();
        this.headers = new String[]{"X-GitHub-Api-Version", API_VERSION, "Authorization", "Bearer " + token.token()};
    }
}
