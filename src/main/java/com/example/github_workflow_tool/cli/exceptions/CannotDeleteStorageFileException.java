package com.example.github_workflow_tool.cli.exceptions;

public class CannotDeleteStorageFileException extends StorageException {

    public CannotDeleteStorageFileException() {
        super("Could not reset the storage file. Please check the permissions.\n" + LOCATION_STRING);
    }
}
