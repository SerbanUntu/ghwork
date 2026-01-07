package com.example.ghwork.api.exceptions;

/**
 * Thrown when an HTTP Server Error occurs (5xx).
 */
public class ServerError extends APIException {

    public ServerError(int code) {
        super(validateAndBuildMessage(code));
    }

    /**
     * Returns an error message indicating that an HTTP Server Error occurred.
     * @param code The status code of the HTTP Server Error response (must be between 500 and 599).
     * @return The error message indicating that an HTTP Server Error occurred.
     * @throws IllegalArgumentException If the entered code is not between 500 and 599 inclusive.
     */
    private static String validateAndBuildMessage(int code) {
        if (code < 500 || code > 599) {
            throw new IllegalArgumentException("ServerError expects a 5xx status code, got: " + code);
        }
        return "An HTTP Server Error occurred. Status code " + code + ".";
    }
}
