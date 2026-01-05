package com.example.github_workflow_tool.api;

/**
 * Throws when there is a URL parsing error, HTTP parsing error, or when the process is interrupted.
 * Should not throw due to an error made by the user.
 */
public class APIException extends RuntimeException {

    public APIException(String message) {
        super("An error occurred and the connection terminated abruptly. Details:\n" + message);
    }
}
