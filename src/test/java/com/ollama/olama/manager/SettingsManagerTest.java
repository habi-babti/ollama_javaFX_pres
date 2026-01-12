package com.ollama.olama.manager;

import com.ollama.olama.model.AppSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsManagerTest {
    
    private SettingsManager settingsManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Override system property to use temp directory for testing
        System.setProperty("user.home", tempDir.toString());
        settingsManager = new SettingsManagerImpl();
    }
    
    @Test
    void shouldReturnDefaultSettingsWhenFileDoesNotExist() {
        // When
        AppSettings settings = settingsManager.loadSettings();
        
        // Then
        assertThat(settings).isEqualTo(AppSettings.defaults());
        assertThat(settings.ollamaBaseUrl()).isEqualTo("http://localhost:11434");
        assertThat(settings.windowWidth()).isEqualTo(900.0);
        assertThat(settings.windowHeight()).isEqualTo(700.0);
        assertThat(settings.windowX()).isEqualTo(-1.0);
        assertThat(settings.windowY()).isEqualTo(-1.0);
        assertThat(settings.lastSelectedModel()).isNull();
        assertThat(settings.systemPrompt()).isNull();
    }
    
    @Test
    void shouldSaveAndLoadSettings() throws IOException {
        // Given
        AppSettings customSettings = new AppSettings(
            "http://custom-host:8080",
            1200.0,
            800.0,
            100.0,
            50.0,
            "llama2:latest",
            "You are a helpful coding assistant.",
            "dark",
            "Arial",
            16
        );
        
        // When
        settingsManager.saveSettings(customSettings);
        AppSettings loadedSettings = settingsManager.loadSettings();
        
        // Then
        assertThat(loadedSettings).isEqualTo(customSettings);
        assertThat(loadedSettings.ollamaBaseUrl()).isEqualTo("http://custom-host:8080");
        assertThat(loadedSettings.windowWidth()).isEqualTo(1200.0);
        assertThat(loadedSettings.windowHeight()).isEqualTo(800.0);
        assertThat(loadedSettings.windowX()).isEqualTo(100.0);
        assertThat(loadedSettings.windowY()).isEqualTo(50.0);
        assertThat(loadedSettings.lastSelectedModel()).isEqualTo("llama2:latest");
        assertThat(loadedSettings.systemPrompt()).isEqualTo("You are a helpful coding assistant.");
    }
    
    @Test
    void shouldCreateSettingsDirectoryIfNotExists() throws IOException {
        // Given
        AppSettings settings = AppSettings.defaults();
        
        // When
        settingsManager.saveSettings(settings);
        
        // Then
        Path settingsDir = tempDir.resolve(".ollama-chat");
        assertThat(Files.exists(settingsDir)).isTrue();
        assertThat(Files.isDirectory(settingsDir)).isTrue();
        
        Path settingsFile = settingsDir.resolve("settings.json");
        assertThat(Files.exists(settingsFile)).isTrue();
        assertThat(Files.isRegularFile(settingsFile)).isTrue();
    }
    
    @Test
    void shouldReturnCorrectSettingsFilePath() {
        // When
        String filePath = settingsManager.getSettingsFilePath();
        
        // Then
        assertThat(filePath).contains(".ollama-chat");
        assertThat(filePath).contains("settings.json");
        assertThat(filePath).startsWith(tempDir.toString());
    }
    
    @Test
    void shouldHandlePartialSettings() throws IOException {
        // Given - settings with some null values
        AppSettings partialSettings = new AppSettings(
            "http://localhost:11434",
            1000.0,
            600.0,
            -1.0,
            -1.0,
            null,
            "Custom system prompt",
            "light",
            "System",
            14
        );
        
        // When
        settingsManager.saveSettings(partialSettings);
        AppSettings loadedSettings = settingsManager.loadSettings();
        
        // Then
        assertThat(loadedSettings).isEqualTo(partialSettings);
        assertThat(loadedSettings.lastSelectedModel()).isNull();
        assertThat(loadedSettings.systemPrompt()).isEqualTo("Custom system prompt");
    }
}