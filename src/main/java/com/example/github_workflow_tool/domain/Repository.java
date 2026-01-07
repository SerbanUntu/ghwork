package com.example.github_workflow_tool.domain;

import com.example.github_workflow_tool.cli.exceptions.InvalidRepositoryStringException;

import java.io.Serializable;
import java.util.*;

/**
 * Stores data about a GitHub repository owner name and repository name.
 */
public class Repository implements Serializable {

    /**
     * The owner of the repository (e.g., "facebook").
     */
    private final String owner;

    /**
     * The repository name (e.g., "react").
     */
    private final String repository;

    /**
     * Constructor which parses repository string into an owner and repository name.
     * @param repositoryString A string in the format "owner/repo".
     * @throws InvalidRepositoryStringException If the format is not respected.
     */
    public Repository(String repositoryString) throws InvalidRepositoryStringException {
        String[] repositoryData = repositoryString.split("/");
        if (repositoryData.length != 2 || repositoryData[0].isBlank() || repositoryData[1].isBlank()) {
            throw new InvalidRepositoryStringException(repositoryString);
        }
        this.owner = repositoryData[0];
        this.repository = repositoryData[1];
    }

    /**
     * Getter for the account name of the repository owner.
     * @return The account name of the repository owner.
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Getter for the repository name.
     * @return The repository name.
     */
    public String getRepository() {
        return this.repository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Repository other = (Repository) o;
        return Objects.equals(this.owner, other.owner) &&
                Objects.equals(this.repository, other.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owner, this.repository);
    }

    /**
     * Returns the repository data in the owner/repo format.
     * @return The repository data in the owner/repo format.
     */
    @Override
    public String toString() {
        return this.owner + "/" + this.repository;
    }
}
