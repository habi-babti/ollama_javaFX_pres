package com.ollama.olama.service;

/**
 * Exception thrown by OllamaService when API operations fail.
 * Provides specific error types and user-friendly messages.
 */
public class OllamaException extends Exception {
    
    /**
     * Types of errors that can occur when communicating with Ollama
     */
    public enum Type {
        CONNECTION_FAILED,
        TIMEOUT,
        MODEL_NOT_FOUND,
        INVALID_REQUEST,
        SERVER_ERROR,
        PARSE_ERROR
    }
    
    private final Type type;
    private final String userMessage;
    
    public OllamaException(Type type, String userMessage) {
        super(userMessage);
        this.type = type;
        this.userMessage = userMessage;
    }
    
    public OllamaException(Type type, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.type = type;
        this.userMessage = userMessage;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
}