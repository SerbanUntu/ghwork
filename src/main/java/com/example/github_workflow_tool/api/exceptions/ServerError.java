package com.example.github_workflow_tool.api.exceptions;

/**
 * Thrown when an HTTP Server Error occurs (5xx).
 */
public class ServerError extends APIException {

    public ServerError(int code) {
        super("An HTTP Server Error occurred. Status code " + code + ".");
        assert code >= 500 && code <= 599;
    }
}
