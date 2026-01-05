package com.example.github_workflow_tool.cli.exceptions;

public abstract class CLIException extends RuntimeException {
    private static final String usageString = "Usage: ghwork <repository name> <access token>";

    public CLIException(String message) {
        super(message + "\n" + CLIException.usageString);
    }
}
