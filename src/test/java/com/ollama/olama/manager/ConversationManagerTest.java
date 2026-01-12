package com.ollama.olama.manager;

import com.ollama.olama.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationManagerTest {
    
    private ConversationManager conversationManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        conversationManager = new ConversationManagerImpl();
    }
    
    @Test
    void shouldSaveAndLoadConversationToFile() throws IOException {
        // Given
        ChatMessage userMessage = ChatMessage.user("Hello, how are you?");
        ChatMessage assistantMessage = ChatMessage.assistant("I'm doing well, thank you!", 1500L);
        String systemPrompt = "You are a helpful assistant.";
        
        conversationManager.addMessage(userMessage);
        conversationManager.addMessage(assistantMessage);
        conversationManager.setSystemPrompt(systemPrompt);
        
        File saveFile = tempDir.resolve("conversation.json").toFile();
        
        // When
        conversationManager.saveToFile(saveFile);
        
        // Create new manager instance to test loading
        ConversationManager newManager = new ConversationManagerImpl();
        newManager.loadFromFile(saveFile);
        
        // Then
        List<ChatMessage> loadedMessages = newManager.getMessages();
        assertThat(loadedMessages).hasSize(2);
        
        ChatMessage loadedUserMessage = loadedMessages.get(0);
        assertThat(loadedUserMessage.role()).isEqualTo("user");
        assertThat(loadedUserMessage.content()).isEqualTo("Hello, how are you?");
        
        ChatMessage loadedAssistantMessage = loadedMessages.get(1);
        assertThat(loadedAssistantMessage.role()).isEqualTo("assistant");
        assertThat(loadedAssistantMessage.content()).isEqualTo("I'm doing well, thank you!");
        assertThat(loadedAssistantMessage.generationTimeMs()).isEqualTo(1500L);
        
        // Verify system prompt is included in API messages
        List<ChatMessage> apiMessages = newManager.getMessagesForApi();
        assertThat(apiMessages).hasSize(3);
        assertThat(apiMessages.get(0).role()).isEqualTo("system");
        assertThat(apiMessages.get(0).content()).isEqualTo(systemPrompt);
    }
    
    @Test
    void shouldSaveAndLoadEmptyConversation() throws IOException {
        // Given
        File saveFile = tempDir.resolve("empty_conversation.json").toFile();
        
        // When
        conversationManager.saveToFile(saveFile);
        
        ConversationManager newManager = new ConversationManagerImpl();
        newManager.loadFromFile(saveFile);
        
        // Then
        assertThat(newManager.getMessages()).isEmpty();
        assertThat(newManager.getMessagesForApi()).isEmpty();
    }
    
    @Test
    void shouldSaveAndLoadConversationWithSystemPromptOnly() throws IOException {
        // Given
        String systemPrompt = "You are a coding assistant.";
        conversationManager.setSystemPrompt(systemPrompt);
        
        File saveFile = tempDir.resolve("system_only_conversation.json").toFile();
        
        // When
        conversationManager.saveToFile(saveFile);
        
        ConversationManager newManager = new ConversationManagerImpl();
        newManager.loadFromFile(saveFile);
        
        // Then
        assertThat(newManager.getMessages()).isEmpty();
        List<ChatMessage> apiMessages = newManager.getMessagesForApi();
        assertThat(apiMessages).hasSize(1);
        assertThat(apiMessages.get(0).role()).isEqualTo("system");
        assertThat(apiMessages.get(0).content()).isEqualTo(systemPrompt);
    }
}