package com.example.github_workflow_tool.cli.exceptions;

public class CannotSaveDataException extends StorageException {

    public CannotSaveDataException() {
        super("Cannot save data to the storage file. Please check permissions.\n" + LOCATION_STRING);
    }
}
