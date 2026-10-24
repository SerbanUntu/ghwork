package com.ghwork.cli;

import com.ghwork.cli.exceptions.EnvException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;
import org.springframework.stereotype.Service;

/**
 * Handles operations with the environment variables of the executable.
 */
@Service
public class EnvService {

    private final Properties properties = new Properties();

    /**
     * Constructor for the service handling environment variables.
     *
     * @throws EnvException If environment loading fails.
     */
    public EnvService() throws EnvException {
        try {
            InputStream resourceStream =
                EnvService.class.getClassLoader().getResourceAsStream("application.properties");
            if (resourceStream == null) {
                throw new EnvException("The application.properties file could not be found");
            }
            this.properties.load(resourceStream);
        } catch (IOException e) {
            throw new EnvException(e.getMessage());
        }
    }

    /**
     * Whether the option to print debug output to the console is set in the environment variables.
     *
     * @return {@code true} if the env file contains "debug=true", {@code false} otherwise
     */
    public boolean isDebugPrintingEnabled() {
        return Objects.equals(this.properties.getProperty("debug"), "true");
    }

    /**
     * Returns the name of the executable.
     *
     * @return The name of the executable, specified by the "name" property in the env file.
     */
    public String getAppName() {
        return this.properties.getProperty("name");
    }

    /**
     * Returns the amount of time after which the API call will fail if it has not yet finished.
     *
     * @return The request timeout.
     */
    public Duration getRequestTimeout() {
        return Duration.of(
            Integer.parseInt(this.properties.getProperty("api.request-timeout-ms")),
            ChronoUnit.MILLIS
        );
    }

    /**
     * Returns the minimum time interval between subsequent calls to the GitHub API.
     *
     * @return The minimum time interval between subsequent calls to the GitHub API.
     */
    public Duration getMinimumRequestInterval() {
        return Duration.of(
            Integer.parseInt(this.properties.getProperty("conn.min-request-interval-ms")),
            ChronoUnit.MILLIS
        );
    }

    /**
     * Returns the amount of time after which the application will fail if a connection to the
     * GitHub API is not yet established.
     *
     * @return The connection timeout.
     */
    public Duration getConnectTimeout() {
        return Duration.of(
            Integer.parseInt(this.properties.getProperty("conn.timeout-ms")),
            ChronoUnit.MILLIS
        );
    }

    /**
     * Returns the maximum number of allowed consecutive failed API requests before crashing the
     * app.
     *
     * @return The maximum number of allowed consecutive failed API requests before crashing the
     *     app.
     */
    public int getMaxRetryAttempts() {
        return Integer.parseInt(this.properties.getProperty("retry.max-attempts"));
    }

    /**
     * Returns the number by which the previous wait time is multiplied after each failed API
     * request.
     *
     * @return The number by which the previous wait time is multiplied after each failed API
     *     request.
     */
    public int getBackoffGrowthFactor() {
        return Integer.parseInt(this.properties.getProperty("retry.factor"));
    }
}
