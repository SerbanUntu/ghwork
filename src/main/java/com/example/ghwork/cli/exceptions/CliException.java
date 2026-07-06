package com.example.ghwork.cli.exceptions;

/**
 * A generic exception caused by running the tool with incorrect arguments.
 */
public abstract class CliException extends RuntimeException {

    /**
     * Informs the user how to run the application in the terminal.
     */
    private static final String usageString = "Usage: ghwork <owner>/<repo> <access token>";

    /**
     * Constructor for a generic exception caused by running the tool with incorrect arguments.
     */
    public CliException(String message) {
        super(message + "\n" + CliException.usageString);
    }
}
