package com.ghwork.cli.exceptions;

/**
 * Thrown when the application cannot save its data to its dedicated file.
 */
public class CannotSaveDataException extends StorageException {

    /**
     * Constructor for the {@link CannotSaveDataException} file system exception.
     */
    public CannotSaveDataException() {
        super(
            "Cannot save data to the storage file. Please check permissions.\n"
                + LOCATION_STRING
        );
    }
}
