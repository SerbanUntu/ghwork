package com.ghwork.cli.exceptions;

/**
 * Thrown when the environment variables are missing, incorrect, or cannot be accessed properly.
 */
public class EnvException extends RuntimeException {

    /**
     * Constructor for an exception thrown when the environment variables are missing, incorrect,
     * or cannot be accessed properly.
     */
    public EnvException(String message) {
        super("Something went wrong when reading the environment data. Details:\n" + message);
    }
}
