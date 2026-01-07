package com.example.github_workflow_tool.cli.exceptions;

public class EnvException extends RuntimeException {
    public EnvException(String message) {
        super("Something went wrong when reading the environment data. Details:\n" + message);
    }
}
