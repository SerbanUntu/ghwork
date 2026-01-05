package com.example.github_workflow_tool.cli;

import com.example.github_workflow_tool.cli.exceptions.CLIException;
import com.example.github_workflow_tool.cli.exceptions.TooFewArgumentsException;
import com.example.github_workflow_tool.cli.exceptions.TooManyArgumentsException;
import com.example.github_workflow_tool.domain.AccessToken;
import com.example.github_workflow_tool.domain.Repository;

public class ArgumentParser {

    public CLIArguments parse(String[] args) throws CLIException {
        if (args == null) {
            throw new TooFewArgumentsException(0);
        }
        if (args.length < 2) {
            throw new TooFewArgumentsException(args.length);
        }
        if (args.length > 2) {
            throw new TooManyArgumentsException(args.length);
        }

        return new CLIArguments(
                new Repository(args[0]),
                new AccessToken(args[1])
        );
    }
}
