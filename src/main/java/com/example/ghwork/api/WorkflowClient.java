package com.example.ghwork.api;

import com.example.ghwork.api.exceptions.ApiException;
import com.example.ghwork.cli.EnvService;
import com.example.ghwork.cli.exceptions.CliException;
import com.example.ghwork.json.JsonService;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * The client responsible for making requests to the "List workflow runs for a repository" route.
 */
@Service
public class WorkflowClient extends GithubClient {

    private static final String route = "/actions/runs";
    private static final Logger LOG = LogManager.getLogger(WorkflowClient.class);

    private HttpRequest request;
    private final JsonService jsonService;

    /**
     * Constructor for a client that fetches information about GitHub Actions workflows.
     *
     * @param envService  The service handling environment variables.
     * @param httpClient  The HTTP client used to make requests.
     * @param jsonService The service handling JSON (de)serialization.
     */
    public WorkflowClient(EnvService envService, HttpClient httpClient, JsonService jsonService) {
        super(envService, httpClient);
        this.jsonService = jsonService;
    }

    /**
     * Build the reused HTTP GET request.
     * Must be called after the repository and access token are set.
     */
    public void initialize() {
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
        if (this.request == null) {
            throw new IllegalStateException(
                "Cannot call WorkflowClient.fetchData() before WorkflowClient.initialize()"
            );
        }
        if (this.envService.isDebugPrintingEnabled()) {
            LOG.debug("GET request: {}", this.request.uri());
        }
        var response = getResponse(this.request);
        return jsonService.parseWorkflowResponse(response.body());
    }
}
