package com.ollama.olama.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ollama.olama.model.AppSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of SettingsManager that persists application settings
 * to a JSON file in the user's home directory.
 */
public class SettingsManagerImpl implements SettingsManager {
    
    private static final String SETTINGS_DIR = ".ollama-chat";
    private static final String SETTINGS_FILE = "settings.json";
    
    private final ObjectMapper objectMapper;
    private final Path settingsPath;
    
    public SettingsManagerImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Create settings path in user home directory
        String userHome = System.getProperty("user.home");
        this.settingsPath = Paths.get(userHome, SETTINGS_DIR, SETTINGS_FILE);
    }
    
    @Override
    public AppSettings loadSettings() {
        try {
            File settingsFile = settingsPath.toFile();
            if (settingsFile.exists() && settingsFile.canRead()) {
                return objectMapper.readValue(settingsFile, AppSettings.class);
            }
        } catch (IOException e) {
            // Log the error but continue with defaults
            System.err.println("Failed to load settings from " + settingsPath + ": " + e.getMessage());
        }
        
        // Return defaults if file doesn't exist or cannot be read
        return AppSettings.defaults();
    }
    
    @Override
    public void saveSettings(AppSettings settings) throws IOException {
        // Create settings directory if it doesn't exist
        Path settingsDir = settingsPath.getParent();
        if (!Files.exists(settingsDir)) {
            Files.createDirectories(settingsDir);
        }
        
        // Write settings to file
        objectMapper.writeValue(settingsPath.toFile(), settings);
    }
    
    @Override
    public String getSettingsFilePath() {
        return settingsPath.toString();
    }
}