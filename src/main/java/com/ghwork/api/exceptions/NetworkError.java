package com.ghwork.api.exceptions;

import java.io.IOException;

/**
 * Thrown when a network error occurs, such as a timeout or a connection reset.
 */
public class NetworkError extends ApiException {

    /**
     * Constructor for a network error.
     *
     * @param cause The I/O exception thrown by the HTTP client.
     */
    public NetworkError(IOException cause) {
        super(cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
    }

    @Override
    public boolean isRetriable() {
        return true;
    }
}
