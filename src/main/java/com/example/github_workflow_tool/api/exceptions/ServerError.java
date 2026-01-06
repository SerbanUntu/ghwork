package com.example.github_workflow_tool.api.exceptions;

/**
 * Thrown when an HTTP Server Error occurs (5xx).
 */
public class ServerError extends APIException {

    public ServerError(int code) {
        super(validateAndBuildMessage(code));
    }

    private static String validateAndBuildMessage(int code) {
        if (code < 500 || code > 599) {
            throw new IllegalArgumentException("ServerError expects a 5xx status code, got: " + code);
        }
        return "An HTTP Server Error occurred. Status code " + code + ".";
    }
}
