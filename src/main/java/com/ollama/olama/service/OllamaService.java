package com.ollama.olama.service;

import com.ollama.olama.model.ChatMessage;
import com.ollama.olama.model.OllamaModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for communicating with the Ollama REST API.
 * Handles model discovery, chat messaging, and connection management.
 */
public interface OllamaService {
    
    /**
     * Fetches list of available models from /api/tags
     * @return CompletableFuture with list of OllamaModel sorted alphabetically by name
     */
    CompletableFuture<List<OllamaModel>> getAvailableModels();
    
    /**
     * Sends a chat request and returns streaming response
     * @param model The model name to use
     * @param messages List of conversation messages
     * @param onToken Callback for each streamed token
     * @return CompletableFuture that completes when streaming finishes with complete ChatMessage
     */
    CompletableFuture<ChatMessage> sendChatMessage(
        String model, 
        List<ChatMessage> messages,
        Consumer<String> onToken
    );
    
    /**
     * Cancels any ongoing request
     */
    void cancelCurrentRequest();
    
    /**
     * Checks if Ollama is reachable
     * @return CompletableFuture with connection status
     */
    CompletableFuture<Boolean> checkConnection();
    
    /**
     * Sets the base URL for Ollama API
     * @param baseUrl The URL (default: http://localhost:11434)
     */
    void setBaseUrl(String baseUrl);
}