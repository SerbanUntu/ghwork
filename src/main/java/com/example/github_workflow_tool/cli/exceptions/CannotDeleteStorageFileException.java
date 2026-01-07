package com.example.github_workflow_tool.cli.exceptions;

/**
 * Thrown when the application cannot delete the file where it stores its data in order to recreate a malformed file.
 */
public class CannotDeleteStorageFileException extends StorageException {

    public CannotDeleteStorageFileException() {
        super("Could not reset the storage file. Please check the permissions.\n" + LOCATION_STRING);
    }
}
