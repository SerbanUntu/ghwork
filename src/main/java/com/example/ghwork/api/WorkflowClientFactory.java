package com.example.ghwork.api;

import com.example.ghwork.cli.EnvService;
import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;
import com.example.ghwork.json.JsonService;
import java.net.http.HttpClient;
import org.springframework.stereotype.Component;

/**
 * Factory pattern for {@link WorkflowClient} objects.
 */
@Component
public class WorkflowClientFactory {

    private final EnvService envService;
    private final HttpClient httpClient;
    private final JsonService jsonService;

    /**
     * Constructor for a factory that builds {@link WorkflowClient} objects.
     *
     * @param envService  The service handling environment variables.
     * @param httpClient  The HTTP client used to make requests.
     * @param jsonService The service handling JSON (de)serialization.
     */
    public WorkflowClientFactory(
        EnvService envService,
        HttpClient httpClient,
        JsonService jsonService
    ) {
        this.envService = envService;
        this.httpClient = httpClient;
        this.jsonService = jsonService;
    }

    /**
     * Build a {@link WorkflowClient} with the given repository and access token.
     *
     * @param repository  The data object containing the GitHub repository data.
     * @param accessToken The user's personal access token.
     * @return A new {@link WorkflowClient} object.
     */
    public WorkflowClient create(Repository repository, AccessToken accessToken) {
        return new WorkflowClient(envService, httpClient, jsonService, repository, accessToken);
    }
}
