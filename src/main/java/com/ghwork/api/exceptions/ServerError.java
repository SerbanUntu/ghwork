package com.ghwork.api.exceptions;

/**
 * Thrown when an HTTP Server Error occurs (5xx).
 */
public class ServerError extends ApiException {

    private final int code;

    /**
     * Constructor for an HTTP server error.
     *
     * @param code The HTTP status code (5xx).
     */
    public ServerError(int code) {
        super(validateAndBuildMessage(code));
        this.code = code;
    }

    /**
     * Returns an error message indicating that an HTTP Server Error occurred.
     *
     * @param code The status code of the HTTP Server Error response (must be between 500 and 599).
     * @return The error message indicating that an HTTP Server Error occurred.
     * @throws IllegalArgumentException If the entered code is not between 500 and 599 inclusive.
     */
    private static String validateAndBuildMessage(int code) {
        if (code < 500 || code > 599) {
            throw new IllegalArgumentException(
                "ServerError expects a 5xx status code, got: " + code);
        }
        return "An HTTP Server Error occurred. Status code " + code + ".";
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return The HTTP status code (5xx).
     */
    public int getCode() {
        return code;
    }

    /**
     * Whether the server error can be retried (500, 502, 503, 504).
     *
     * @return {@code true} if the request can be retried, {@code false} otherwise.
     */
    @Override
    public boolean isRetriable() {
        return code == 500 || code == 502 || code == 503 || code == 504;
    }
}
