package com.ollama.olama.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollama.olama.model.ChatMessage;
import com.ollama.olama.model.OllamaModel;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Implementation of OllamaService using Java's HttpClient for API communication.
 * Handles model discovery, chat messaging with streaming, and connection management.
 */
public class OllamaServiceImpl implements OllamaService {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String baseUrl = "http://localhost:11434";
    private HttpRequest currentRequest;
    
    public OllamaServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public CompletableFuture<List<OllamaModel>> getAvailableModels() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/tags"))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::parseModelsResponse)
            .thenApply(this::sortModelsAlphabetically)
            .exceptionally(this::handleException);
    }
    
    @Override
    public CompletableFuture<ChatMessage> sendChatMessage(
            String model, 
            List<ChatMessage> messages,
            Consumer<String> onToken) {
        
        try {
            // Build request body
            var requestBody = buildChatRequestBody(model, messages);
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofMinutes(5))
                .build();
                
            this.currentRequest = request;
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenApply(response -> {
                    if (response.statusCode() == 404) {
                        throw new RuntimeException(new OllamaException(
                            OllamaException.Type.MODEL_NOT_FOUND,
                            "Model '" + model + "' not found. Please select a different model."
                        ));
                    } else if (response.statusCode() == 400) {
                        throw new RuntimeException(new OllamaException(
                            OllamaException.Type.INVALID_REQUEST,
                            "Invalid request format."
                        ));
                    } else if (response.statusCode() >= 500) {
                        throw new RuntimeException(new OllamaException(
                            OllamaException.Type.SERVER_ERROR,
                            "Ollama server error. Please try again."
                        ));
                    }
                    return processStreamingResponse(response, onToken);
                })
                .exceptionally(this::handleChatException);
                
        } catch (Exception e) {
            return CompletableFuture.failedFuture(
                new OllamaException(OllamaException.Type.INVALID_REQUEST, "Failed to build request", e)
            );
        }
    }
    
    @Override
    public void cancelCurrentRequest() {
        // Note: HttpClient doesn't provide direct request cancellation
        // In a real implementation, we would need to track the CompletableFuture
        // and cancel it, or use a different HTTP client that supports cancellation
        if (currentRequest != null) {
            // Reset current request
            currentRequest = null;
        }
    }
    
    @Override
    public CompletableFuture<Boolean> checkConnection() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/tags"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> response.statusCode() == 200)
            .exceptionally(throwable -> false);
    }
    
    @Override
    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        
        // Remove trailing slash if present
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
    
    /**
     * Parses the JSON response from /api/tags endpoint into List<OllamaModel>
     */
    private List<OllamaModel> parseModelsResponse(HttpResponse<String> response) {
        try {
            if (response.statusCode() != 200) {
                throw new OllamaException(
                    OllamaException.Type.SERVER_ERROR,
                    "Failed to fetch models. Status: " + response.statusCode()
                );
            }
            
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode modelsArray = root.get("models");
            
            List<OllamaModel> models = new ArrayList<>();
            if (modelsArray != null && modelsArray.isArray()) {
                for (JsonNode modelNode : modelsArray) {
                    models.add(OllamaModel.fromJson(modelNode));
                }
            }
            
            return models;
        } catch (IOException e) {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.PARSE_ERROR,
                "Failed to parse models response", e
            ));
        } catch (OllamaException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Sorts models alphabetically by name
     */
    private List<OllamaModel> sortModelsAlphabetically(List<OllamaModel> models) {
        return models.stream()
            .sorted(Comparator.comparing(OllamaModel::name))
            .toList();
    }
    
    /**
     * Builds the request body for chat API call
     */
    private Map<String, Object> buildChatRequestBody(String model, List<ChatMessage> messages) {
        List<Map<String, String>> apiMessages = messages.stream()
            .map(ChatMessage::toApiFormat)
            .toList();
            
        return Map.of(
            "model", model,
            "messages", apiMessages,
            "stream", true
        );
    }
    
    /**
     * Processes streaming NDJSON response and calls onToken for each token
     */
    private ChatMessage processStreamingResponse(HttpResponse<Stream<String>> response, Consumer<String> onToken) {
        StringBuilder completeContent = new StringBuilder();
        long startTime = System.currentTimeMillis();
        
        try {
            response.body().forEach(line -> {
                if (!line.trim().isEmpty()) {
                    try {
                        JsonNode json = objectMapper.readTree(line);
                        JsonNode message = json.get("message");
                        
                        if (message != null) {
                            String content = message.get("content").asText();
                            if (!content.isEmpty()) {
                                completeContent.append(content);
                                onToken.accept(content);
                            }
                        }
                        
                        // Check if this is the final message
                        if (json.has("done") && json.get("done").asBoolean()) {
                            // Response is complete
                            return;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(new OllamaException(
                            OllamaException.Type.PARSE_ERROR,
                            "Failed to parse streaming response", e
                        ));
                    }
                }
            });
            
            long generationTime = System.currentTimeMillis() - startTime;
            return ChatMessage.assistant(completeContent.toString(), generationTime);
            
        } catch (Exception e) {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.PARSE_ERROR,
                "Failed to process streaming response", e
            ));
        }
    }
    
    /**
     * Handles exceptions for getAvailableModels
     */
    private <T> T handleException(Throwable throwable) {
        if (throwable.getCause() instanceof ConnectException) {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.CONNECTION_FAILED,
                "Cannot connect to Ollama. Please ensure Ollama is running.",
                throwable
            ));
        } else if (throwable.getCause() instanceof HttpTimeoutException) {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.TIMEOUT,
                "Request timed out. Ollama may be busy.",
                throwable
            ));
        } else {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.SERVER_ERROR,
                "Unexpected error occurred: " + throwable.getMessage(),
                throwable
            ));
        }
    }
    
    /**
     * Handles exceptions for sendChatMessage
     */
    private ChatMessage handleChatException(Throwable throwable) {
        if (throwable.getCause() instanceof OllamaException) {
            throw new RuntimeException(throwable.getCause());
        } else if (throwable.getCause() instanceof ConnectException) {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.CONNECTION_FAILED,
                "Cannot connect to Ollama. Please ensure Ollama is running.",
                throwable
            ));
        } else if (throwable.getCause() instanceof HttpTimeoutException) {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.TIMEOUT,
                "Request timed out. Ollama may be busy.",
                throwable
            ));
        } else {
            throw new RuntimeException(new OllamaException(
                OllamaException.Type.SERVER_ERROR,
                "Unexpected error occurred: " + throwable.getMessage(),
                throwable
            ));
        }
    }
}