package com.ghwork.cli.exceptions;

/**
 * Thrown when the user enters an invalid GitHub personal access token in the command line.
 */
public class InvalidAccessTokenException extends CliException {

    /**
     * Constructor for an exception thrown when the user enters an invalid
     * GitHub personal access token in the command line.
     */
    public InvalidAccessTokenException() {
        super("The token you provided is not a valid GitHub personal access token.");
    }
}
