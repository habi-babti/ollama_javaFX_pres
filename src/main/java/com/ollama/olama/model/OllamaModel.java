package com.ollama.olama.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents an Ollama model with name, size information, and modification date.
 * Provides display formatting and JSON parsing capabilities.
 */
public record OllamaModel(
    String name,
    String size,
    long sizeBytes,
    String modifiedAt
) {
    
    /**
     * Returns display format: "name (size)"
     */
    @Override
    public String toString() {
        return name + " (" + size + ")";
    }
    
    /**
     * Creates OllamaModel from JSON response from /api/tags endpoint
     */
    public static OllamaModel fromJson(JsonNode json) {
        String name = json.get("name").asText();
        long sizeBytes = json.get("size").asLong();
        String modifiedAt = json.get("modified_at").asText();
        
        // Convert bytes to human-readable format
        String size = formatBytes(sizeBytes);
        
        return new OllamaModel(name, size, sizeBytes, modifiedAt);
    }
    
    /**
     * Converts bytes to human-readable format (e.g., "4.1 GB")
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
}