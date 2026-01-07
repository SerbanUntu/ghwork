package com.example.ghwork.cli.exceptions;

/**
 * Thrown when the environment variables are missing, incorrect, or cannot be accessed properly.
 */
public class EnvException extends RuntimeException {

    public EnvException(String message) {
        super("Something went wrong when reading the environment data. Details:\n" + message);
    }
}
