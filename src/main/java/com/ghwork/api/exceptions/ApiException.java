package com.ghwork.api.exceptions;

/**
 * Thrown when there is a URL parsing error, HTTP parsing error, or when the process is interrupted.
 * Should not throw due to an error made by the user.
 */
public class ApiException extends RuntimeException {

    /**
     * Constructor for an API Exception.
     *
     * @param message The message describing the type of connection error that happened.
     */
    public ApiException(String message) {
        super("An error occurred and the connection terminated abruptly. Details:\n" + message);
    }
}
