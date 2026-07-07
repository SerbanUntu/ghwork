package com.example.ghwork.cli.exceptions;

import com.example.ghwork.domain.Repository;

/**
 * Thrown when the user enters a repository that does not exist.
 */
public class InexistentRepositoryException extends CliException {

    /**
     * Constructor for an exception thrown when the user enters a repository that does not exist.
     */
    public InexistentRepositoryException(Repository repository) {
        super(
            "The repository you entered does not exist, or you do not have access to it: "
                + repository
        );
    }
}
