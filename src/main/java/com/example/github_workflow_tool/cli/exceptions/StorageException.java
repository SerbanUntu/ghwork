package com.example.github_workflow_tool.cli.exceptions;

public abstract class StorageException extends RuntimeException {

    protected static final String LOCATION_STRING =
            "Location: Windows -> AppData\\Roaming, Mac -> Library/Application Support, Unix -> .config";

    public StorageException(String message) {
        super(message);
    }

}
