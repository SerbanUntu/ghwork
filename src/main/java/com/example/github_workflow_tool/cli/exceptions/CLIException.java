package com.example.github_workflow_tool.cli.exceptions;

/**
 * A generic exception caused by running the tool with incorrect arguments.
 */
public abstract class CLIException extends RuntimeException {

    /**
     * Informs the user how to run the application in the terminal
     */
    private static final String usageString = "Usage: ghwork <owner>/<repo> <access token>";

    public CLIException(String message) {
        super(message + "\n" + CLIException.usageString);
    }
}
