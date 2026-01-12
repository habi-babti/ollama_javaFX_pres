package com.ollama.olama.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Application settings including Ollama configuration, window state, and user preferences.
 * Provides default values for initial application setup.
 */
public record AppSettings(
    String ollamaBaseUrl,
    double windowWidth,
    double windowHeight,
    double windowX,
    double windowY,
    String lastSelectedModel,
    String systemPrompt,
    String theme,
    String fontFamily,
    int fontSize
) {
    
    @JsonCreator
    public AppSettings(
        @JsonProperty("ollamaBaseUrl") String ollamaBaseUrl,
        @JsonProperty("windowWidth") double windowWidth,
        @JsonProperty("windowHeight") double windowHeight,
        @JsonProperty("windowX") double windowX,
        @JsonProperty("windowY") double windowY,
        @JsonProperty("lastSelectedModel") String lastSelectedModel,
        @JsonProperty("systemPrompt") String systemPrompt,
        @JsonProperty("theme") String theme,
        @JsonProperty("fontFamily") String fontFamily,
        @JsonProperty("fontSize") int fontSize
    ) {
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.windowX = windowX;
        this.windowY = windowY;
        this.lastSelectedModel = lastSelectedModel;
        this.systemPrompt = systemPrompt;
        this.theme = theme != null ? theme : "light";
        this.fontFamily = fontFamily != null ? fontFamily : "System";
        this.fontSize = fontSize > 0 ? fontSize : 14;
    }
    
    /**
     * Creates default application settings
     */
    public static AppSettings defaults() {
        return new AppSettings(
            "http://localhost:11434",
            900.0,
            700.0,
            -1.0,
            -1.0,
            null,
            null,
            "light",
            "System",
            14
        );
    }
}