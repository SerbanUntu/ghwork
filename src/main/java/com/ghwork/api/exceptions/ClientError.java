package com.ghwork.api.exceptions;

/**
 * Thrown when an HTTP Client Error occurs (4xx).
 */
public class ClientError extends ApiException {

    private final int code;

    /**
     * Constructor for an HTTP client error.
     *
     * @param code The HTTP status code (4xx).
     */
    public ClientError(int code) {
        super(validateAndBuildMessage(code));
        this.code = code;
    }

    /**
     * Returns an error message indicating that an HTTP Client Error occurred.
     *
     * @param code The status code of the HTTP Client Error response (must be between 400 and 499).
     * @return The error message indicating that an HTTP Client Error occurred.
     * @throws IllegalArgumentException If the entered code is not between 400 and 499 inclusive.
     */
    private static String validateAndBuildMessage(int code) {
        if (code < 400 || code > 499) {
            throw new IllegalArgumentException(
                "ClientError expects a 4xx status code, got: " + code);
        }
        return "An HTTP Client Error occurred. Status code " + code + ".";
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return The HTTP status code (4xx).
     */
    public int getCode() {
        return code;
    }

    /**
     * Whether the client error is retriable (408 or 429).
     *
     * @return {@code true} if the request can be retried, {@code false} otherwise.
     */
    @Override
    public boolean isRetriable() {
        return code == 408 || code == 429;
    }
}
