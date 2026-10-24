package com.ghwork.api.exceptions;

/**
 * Thrown when there is a URL parsing error, HTTP parsing error, or when the process is interrupted.
 * Should not throw due to an error made by the user.
 */
public class ApiException extends RuntimeException {

    private static final String PREFIX =
        "An error occurred and the connection terminated abruptly. Details:\n";

    /**
     * Constructor for an API Exception.
     *
     * @param message The message describing the type of connection error that happened.
     */
    public ApiException(String message) {
        super(PREFIX + message);
    }

    /**
     * Constructor for an API Exception caused by another exception.
     *
     * @param message The message describing the type of connection error that happened.
     * @param cause The underlying exception.
     */
    public ApiException(String message, Throwable cause) {
        super(PREFIX + message, cause);
    }

    /**
     * Whether repeating the request that caused this exception could succeed.
     *
     * @return {@code true} if the request can be retried, {@code false} otherwise.
     */
    public boolean isRetriable() {
        return false;
    }
}
