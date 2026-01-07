package com.example.github_workflow_tool.cli;

import com.example.github_workflow_tool.cli.exceptions.EnvException;

import java.io.IOException;
import java.util.*;

public class EnvService {

    private final Properties properties = new Properties();

    public EnvService() throws EnvException {
        try {
            this.properties.load(EnvService.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            throw new EnvException(e.getMessage());
        }
    }

    public boolean isDebugPrintingEnabled() {
        return Objects.equals(this.properties.getProperty("debug"), "true");
    }

    public String getAppName() {
        return this.properties.getProperty("name");
    }
}
