package com.ollama.olama.manager;

import com.ollama.olama.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationSerializationTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void shouldCreateValidJsonFormat() throws IOException {
        // Given
        ConversationManager manager = new ConversationManagerImpl();
        manager.addMessage(ChatMessage.user("Hello"));
        manager.addMessage(ChatMessage.assistant("Hi there!", 1000L));
        manager.setSystemPrompt("You are helpful");
        
        File saveFile = tempDir.resolve("test.json").toFile();
        
        // When
        manager.saveToFile(saveFile);
        
        // Then
        String jsonContent = Files.readString(saveFile.toPath());
        System.out.println("Generated JSON:");
        System.out.println(jsonContent);
        
        // Verify JSON contains expected structure
        assertThat(jsonContent).contains("\"messages\"");
        assertThat(jsonContent).contains("\"systemPrompt\"");
        assertThat(jsonContent).contains("\"role\":\"user\"");
        assertThat(jsonContent).contains("\"role\":\"assistant\"");
        assertThat(jsonContent).contains("\"content\":\"Hello\"");
        assertThat(jsonContent).contains("\"content\":\"Hi there!\"");
        assertThat(jsonContent).contains("\"generationTimeMs\":1000");
        assertThat(jsonContent).contains("\"systemPrompt\":\"You are helpful\"");
    }
}