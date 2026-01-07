package com.example.github_workflow_tool.cli;

import com.example.github_workflow_tool.cli.exceptions.CannotCreateStorageFileException;
import com.example.github_workflow_tool.cli.exceptions.CannotDeleteStorageFileException;
import com.example.github_workflow_tool.cli.exceptions.CannotSaveDataException;
import com.example.github_workflow_tool.cli.exceptions.StorageException;
import com.example.github_workflow_tool.domain.Repository;
import com.example.github_workflow_tool.domain.ToolState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StorageService {

    private final Path appDir;
    private final Path filePath;
    private final EnvService envService = new EnvService();
    private static final String FILENAME = "data.ser";

    public StorageService() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String userHome = System.getProperty("user.home");
        Path path;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            path = (appData != null) ? Paths.get(appData) : Paths.get(userHome, "AppData", "Roaming");
        } else if (os.contains("mac")) {
            path = Paths.get(userHome, "Library", "Application Support");
        } else {
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            path = (xdgConfig != null) ? Paths.get(xdgConfig) : Paths.get(userHome, ".config");
        }

        this.appDir = path.resolve(this.envService.getAppName());
        this.filePath = this.appDir.resolve(FILENAME);
    }

    private Map<Repository, ToolState> tryCastingInput(Object inputObject) throws ClassCastException {
        if (inputObject instanceof Map<?, ?> objectMap) {
            boolean isValidMap = objectMap
                    .entrySet()
                    .stream()
                    .allMatch(e -> e.getKey() instanceof Repository && e.getValue() instanceof ToolState);
            if (isValidMap) {
                @SuppressWarnings("unchecked")
                var castedMap = (Map<Repository, ToolState>) objectMap;
                return castedMap;
            }
        }
        throw new ClassCastException("Read object cannot be cast into a map of repositories to tool states.");
    }

    private void recoverFiles() throws StorageException {
        try {
            Files.deleteIfExists(this.filePath);
        } catch (IOException ePrime) {
            throw new CannotDeleteStorageFileException();
        }
    }

    public Map<Repository, ToolState> retrieve() throws StorageException {
        try {
            if (!Files.exists(this.appDir)) {
                Files.createDirectory(this.appDir);
            }
            if (!Files.exists(this.filePath)) {
                Files.createFile(this.filePath);
                var result = new HashMap<Repository, ToolState>();
                this.save(result);
                return result;
            }
        } catch (IOException e) {
            throw new CannotCreateStorageFileException();
        }

        try (FileInputStream fileInputStream = new FileInputStream(this.filePath.toFile())) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                Object inputObject = objectInputStream.readObject();
                Map<Repository, ToolState> castedInput = tryCastingInput(inputObject);

                if (this.envService.isDebugPrintingEnabled()) {
                    System.out.println("[DEBUG] Read from file: " + this.filePath);
                }
                return castedInput;
            }
        } catch (ClassNotFoundException | ClassCastException | IOException e) {
            recoverFiles();
            return new HashMap<>();
        }
    }

    public void save(Map<Repository, ToolState> toolStateByRepo) throws StorageException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.filePath.toFile())) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(toolStateByRepo);
                objectOutputStream.flush();

                if (this.envService.isDebugPrintingEnabled()) {
                    System.out.println("[DEBUG] Saved to file: " + this.filePath);
                }
            }
        } catch (IOException e) {
            throw new CannotSaveDataException();
        }
    }
}
