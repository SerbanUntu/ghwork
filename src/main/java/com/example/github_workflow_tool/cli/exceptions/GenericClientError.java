package com.example.github_workflow_tool.cli.exceptions;

/**
 * Thrown when the server returns a 4xx code that is not handled more specifically.
 */
public class GenericClientError extends CLIException {

    public GenericClientError(int code) {
        super(validateAndBuildMessage(code));
    }

    /**
     * Returns an error message indicating that an HTTP Client Error occurred.
     * @param code The status code of the HTTP Client Error response (must be between 400 and 499).
     * @return The error message indicating that an HTTP Client Error occurred.
     * @throws IllegalArgumentException If the entered code is not between 400 and 499 inclusive.
     */
    private static String validateAndBuildMessage(int code) {
        if (code < 400 || code > 499) {
            throw new IllegalArgumentException("GenericClientError expects a 4xx status code, got: " + code);
        }
        return "The status code " + code + " was returned from the server. " +
                "Please make sure the repository string and access token you entered are correct.";
    }
}
