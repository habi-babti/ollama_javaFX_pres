package com.ollama.olama.manager;

import com.ollama.olama.model.AppSettings;

import java.io.IOException;

/**
 * Manages application settings persistence to the user's home directory.
 * Handles loading and saving of window state, Ollama configuration, and user preferences.
 */
public interface SettingsManager {
    
    /**
     * Loads application settings from the settings file.
     * If the file doesn't exist or cannot be read, returns default settings.
     * 
     * @return AppSettings loaded from file or defaults
     */
    AppSettings loadSettings();
    
    /**
     * Saves application settings to the settings file.
     * Creates the settings directory if it doesn't exist.
     * 
     * @param settings The settings to save
     * @throws IOException if the settings cannot be saved
     */
    void saveSettings(AppSettings settings) throws IOException;
    
    /**
     * Gets the path to the settings file.
     * 
     * @return The full path to the settings file
     */
    String getSettingsFilePath();
}