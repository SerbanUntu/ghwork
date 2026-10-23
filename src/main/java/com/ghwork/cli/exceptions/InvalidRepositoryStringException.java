package com.ghwork.cli.exceptions;

/**
 * Thrown when the user enters a repository string that doesn't have the owner/repo format.
 */
public class InvalidRepositoryStringException extends CliException {

    /**
     * Constructor for an exception thrown when the user enters a repository string that
     * doesn't have the owner/repo format.
     */
    public InvalidRepositoryStringException(String repositoryString) {
        super("The owner/repo pair you provided is not valid: " + repositoryString);
    }
}
