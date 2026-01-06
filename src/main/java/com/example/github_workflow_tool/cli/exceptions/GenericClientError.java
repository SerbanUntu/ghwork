package com.example.github_workflow_tool.cli.exceptions;

/**
 * Thrown when the server returns a 4xx code that is not handled more specifically.
 */
public class GenericClientError extends CLIException {

    public GenericClientError(int code) {
        super(validateAndBuildMessage(code));
    }

    private static String validateAndBuildMessage(int code) {
        if (code < 400 || code > 499) {
            throw new IllegalArgumentException("GenericClientError expects a 4xx status code, got: " + code);
        }
        return "The status code " + code + " was returned from the server. " +
                "Please make sure the repository string and access token you entered are correct.";
    }
}
