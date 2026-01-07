package com.example.ghwork.cli;

import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;

/**
 * A value object for the correctly parsed arguments from the command line that the tool needs.
 * @param repository An object encapsulating the owner and repository name of a GitHub repository.
 * @param accessToken The personal GitHub access token of the user.
 */
public record CLIArguments(Repository repository, AccessToken accessToken) {
}
