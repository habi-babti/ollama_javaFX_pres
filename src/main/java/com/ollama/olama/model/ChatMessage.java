package com.ollama.olama.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a chat message with role, content, timestamp, and optional generation time.
 * Supports conversion to Ollama API format.
 */
public record ChatMessage(
    String role,
    String content,
    LocalDateTime timestamp,
    Long generationTimeMs
) {
    
    @JsonCreator
    public ChatMessage(
        @JsonProperty("role") String role,
        @JsonProperty("content") String content,
        @JsonProperty("timestamp") LocalDateTime timestamp,
        @JsonProperty("generationTimeMs") Long generationTimeMs
    ) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.generationTimeMs = generationTimeMs;
    }
    
    /**
     * Creates a user message with current timestamp
     */
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, LocalDateTime.now(), null);
    }
    
    /**
     * Creates an assistant message with current timestamp and generation time
     */
    public static ChatMessage assistant(String content, long generationTimeMs) {
        return new ChatMessage("assistant", content, LocalDateTime.now(), generationTimeMs);
    }
    
    /**
     * Creates a system message with current timestamp
     */
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, LocalDateTime.now(), null);
    }
    
    /**
     * Converts to JSON format for Ollama API requests
     */
    public Map<String, String> toApiFormat() {
        return Map.of("role", role, "content", content);
    }
}