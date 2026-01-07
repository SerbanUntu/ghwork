package com.example.github_workflow_tool.cli.exceptions;

/**
 * Thrown when the user enters a repository string that doesn't have the owner/repo format.
 */
public class InvalidRepositoryStringException extends CLIException {

    public InvalidRepositoryStringException(String repositoryString) {
        super("The owner/repo pair you provided is not valid: " + repositoryString);
    }
}
