package com.ollama.olama.manager;

import com.ollama.olama.model.ChatMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Manages the current conversation state and history.
 */
public interface ConversationManager {
    
    /**
     * Adds a message to the current conversation
     */
    void addMessage(ChatMessage message);
    
    /**
     * Gets all messages in the current conversation
     */
    List<ChatMessage> getMessages();
    
    /**
     * Clears the current conversation
     */
    void clearConversation();
    
    /**
     * Gets the current conversation for API requests (includes system prompt)
     */
    List<ChatMessage> getMessagesForApi();
    
    /**
     * Sets the system prompt
     */
    void setSystemPrompt(String prompt);
    
    /**
     * Saves conversation to file
     */
    void saveToFile(File file) throws IOException;
    
    /**
     * Loads conversation from file
     */
    void loadFromFile(File file) throws IOException;
}