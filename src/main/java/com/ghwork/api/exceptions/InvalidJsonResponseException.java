package com.ghwork.api.exceptions;

/**
 * Thrown when the body of a successful response cannot be parsed into JSON properly.
 */
public class InvalidJsonResponseException extends ApiException {

    /**
     * Constructor for an invalid JSON response exception.
     *
     * @param expectedType The name of the data object the response should have been parsed as.
     * @param details What was wrong with the response.
     * @param cause The underlying parsing exception (can be {@code null}).
     */
    public InvalidJsonResponseException(String expectedType, String details, Throwable cause) {
        super("The response could not be parsed as " + expectedType + ": " + details, cause);
    }

    /**
     * Whether invalid JSON response exceptions can be retried (they can).
     *
     * @return {@code true}, since the request can be retried.
     */
    @Override
    public boolean isRetriable() {
        return true;
    }
}
