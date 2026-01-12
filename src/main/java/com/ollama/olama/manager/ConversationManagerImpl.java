package com.ollama.olama.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ollama.olama.model.ChatMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ConversationManager that manages chat history and provides
 * JSON serialization for conversation persistence.
 */
public class ConversationManagerImpl implements ConversationManager {
    
    private final List<ChatMessage> messages;
    private String systemPrompt;
    private final ObjectMapper objectMapper;
    
    public ConversationManagerImpl() {
        this.messages = new ArrayList<>();
        this.systemPrompt = null;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void addMessage(ChatMessage message) {
        messages.add(message);
    }
    
    @Override
    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }
    
    @Override
    public void clearConversation() {
        messages.clear();
    }
    
    @Override
    public List<ChatMessage> getMessagesForApi() {
        List<ChatMessage> apiMessages = new ArrayList<>();
        
        // Add system prompt first if it exists
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            apiMessages.add(ChatMessage.system(systemPrompt));
        }
        
        // Add all conversation messages
        apiMessages.addAll(messages);
        
        return apiMessages;
    }
    
    @Override
    public void setSystemPrompt(String prompt) {
        this.systemPrompt = prompt;
    }
    
    @Override
    public void saveToFile(File file) throws IOException {
        ConversationData data = new ConversationData(messages, systemPrompt);
        objectMapper.writeValue(file, data);
    }
    
    @Override
    public void loadFromFile(File file) throws IOException {
        ConversationData data = objectMapper.readValue(file, ConversationData.class);
        messages.clear();
        messages.addAll(data.messages());
        this.systemPrompt = data.systemPrompt();
    }
    
    /**
     * Data class for JSON serialization of conversation state
     */
    private record ConversationData(
        List<ChatMessage> messages,
        String systemPrompt
    ) {}
}