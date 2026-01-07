package com.example.ghwork.cli.exceptions;

/**
 * Thrown when the application cannot save its data to its dedicated file.
 */
public class CannotSaveDataException extends StorageException {

    public CannotSaveDataException() {
        super("Cannot save data to the storage file. Please check permissions.\n" + LOCATION_STRING);
    }
}
