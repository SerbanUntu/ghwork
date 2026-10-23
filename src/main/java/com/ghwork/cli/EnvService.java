package com.ghwork.cli;

import com.ghwork.cli.exceptions.EnvException;
import java.io.IOException;
import java.io.InputStream;
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
}
