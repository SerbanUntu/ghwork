package com.example.github_workflow_tool.cli.exceptions;

public class CannotCreateStorageFileException extends StorageException {

    public CannotCreateStorageFileException() {
        super("The tool could not create the file to store data in. Please check permissions.\n" + LOCATION_STRING);
    }
}
