package com.ghwork.cli.exceptions;

/**
 * Thrown when the application cannot delete the file where it stores its data
 * in order to recreate a malformed file.
 */
public class CannotDeleteStorageFileException extends StorageException {

    /**
     * Constructor for the {@link CannotDeleteStorageFileException} file system exception.
     */
    public CannotDeleteStorageFileException() {
        super(
            "Could not reset the storage file. Please check the permissions.\n"
                + LOCATION_STRING
        );
    }
}
