package com.example.ghwork.cli.exceptions;

/**
 * Thrown when a fault occurs when trying to read/write application-specific data from/to the file system.
 */
public abstract class StorageException extends RuntimeException {

    /**
     * Tells the user where the application-specific data is stored on their device.
     */
    protected static final String LOCATION_STRING =
            "Location: Windows -> %APPDATA%, Mac -> ~/Library/Application Support, Unix -> ~/.config";

    public StorageException(String message) {
        super(message);
    }

}
