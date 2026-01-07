package com.example.ghwork.cli;

import com.example.ghwork.cli.exceptions.CliException;
import com.example.ghwork.cli.exceptions.TooFewArgumentsException;
import com.example.ghwork.cli.exceptions.TooManyArgumentsException;
import com.example.ghwork.domain.AccessToken;
import com.example.ghwork.domain.Repository;
import java.util.Arrays;
import org.springframework.stereotype.Service;

/**
 * Handles validation of command line arguments entered by the user. Acts as a singleton.
 */
@Service
public class ArgumentParser {

    /**
     * Parse a list of command-line arguments.
     *
     * @param args The command line arguments entered by the user.
     * @return The parsed command line arguments as a value object.
     * @throws CliException If there are not exactly two non-empty arguments,
     *                      or if the repository string format is invalid.
     */
    public CliArguments parse(String[] args) throws CliException {
        if (args == null) {
            throw new TooFewArgumentsException(0);
        }
        String[] nonEmptyArgs = Arrays.stream(args)
            .filter(arg -> arg != null && !arg.isBlank())
            .toArray(String[]::new);
        if (nonEmptyArgs.length < 2) {
            throw new TooFewArgumentsException(nonEmptyArgs.length);
        }
        if (nonEmptyArgs.length > 2) {
            throw new TooManyArgumentsException(nonEmptyArgs.length);
        }

        return new CliArguments(
            new Repository(nonEmptyArgs[0]),
            new AccessToken(nonEmptyArgs[1])
        );
    }
}
