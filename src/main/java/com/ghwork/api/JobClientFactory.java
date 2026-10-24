package com.ghwork.api;

import com.ghwork.cli.EnvService;
import com.ghwork.domain.AccessToken;
import com.ghwork.domain.Repository;
import com.ghwork.json.JsonService;
import java.net.http.HttpClient;
import org.springframework.stereotype.Component;

/**
 * Factory pattern for {@link JobClient} objects.
 */
@Component
public class JobClientFactory {

    private final EnvService envService;
    private final HttpClient httpClient;
    private final JsonService jsonService;

    /**
     * Constructor for a factory that builds {@link JobClient} objects.
     *
     * @param envService  The service handling environment variables.
     * @param httpClient  The HTTP client used to make requests.
     * @param jsonService The service handling JSON (de)serialization.
     */
    public JobClientFactory(EnvService envService, HttpClient httpClient, JsonService jsonService) {
        this.envService = envService;
        this.httpClient = httpClient;
        this.jsonService = jsonService;
    }

    /**
     * Build a {@link JobClient} with the given repository and access token.
     *
     * @param repository  The data object containing the GitHub repository data.
     * @param accessToken The user's personal access token.
     * @param apiBaseRoute The protocol and domain of the URL.
     * @return A new {@link JobClient} object.
     */
    public JobClient create(Repository repository, AccessToken accessToken, String apiBaseRoute) {
        return new JobClient(
            envService,
            httpClient,
            jsonService,
            repository,
            accessToken,
            apiBaseRoute
        );
    }
}
