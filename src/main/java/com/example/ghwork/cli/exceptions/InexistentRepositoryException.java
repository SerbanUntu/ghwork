package com.example.ghwork.cli.exceptions;

import com.example.ghwork.domain.Repository;

/**
 * Thrown when the user enters a repository that does not exist
 */
public class InexistentRepositoryException extends CLIException {

    public InexistentRepositoryException(Repository repository) {
        super("The repository you entered does not exist, or you do not have access to it: " + repository);
    }
}
