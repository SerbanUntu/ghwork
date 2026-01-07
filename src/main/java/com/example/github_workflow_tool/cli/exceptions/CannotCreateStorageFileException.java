package com.example.github_workflow_tool.cli.exceptions;

/**
 * Thrown when the application cannot create the necessary files and directories to store its data to the file system.
 */
public class CannotCreateStorageFileException extends StorageException {

    public CannotCreateStorageFileException() {
        super("The tool could not create the file to store data in. Please check permissions.\n" + LOCATION_STRING);
    }
}
