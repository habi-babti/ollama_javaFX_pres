package com.ollama.olama.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Property-based tests for ChatMessage timestamp ordering and validation.
 */
@Tag("Feature: ollama-chat, Property 4: Chronological Message Ordering with Timestamps")
class ChatMessagePropertyTest {

    /**
     * Property 4: Chronological Message Ordering with Timestamps
     * For any sequence of messages created over time, they should maintain chronological order
     * and each message should have a non-null timestamp.
     * 
     * Validates: Requirements 4.1, 4.7
     */
    @Property(tries = 100)
    void messagesCreatedSequentiallyMaintainChronologicalOrder(
        @ForAll @IntRange(min = 1, max = 20) int messageCount,
        @ForAll("messageContents") List<String> contents
    ) {
        Assume.that(contents.size() >= messageCount);
        
        List<ChatMessage> messages = new ArrayList<>();
        LocalDateTime previousTimestamp = null;
        
        // Create messages sequentially with small delays to ensure timestamp ordering
        for (int i = 0; i < messageCount; i++) {
            String content = contents.get(i % contents.size());
            
            // Create different types of messages to test all factory methods
            ChatMessage message = switch (i % 3) {
                case 0 -> ChatMessage.user(content);
                case 1 -> ChatMessage.assistant(content, 100L + i * 10);
                case 2 -> ChatMessage.system(content);
                default -> throw new IllegalStateException("Unexpected value: " + (i % 3));
            };
            
            messages.add(message);
            
            // Verify each message has a non-null timestamp (Requirement 4.7)
            assertThat(message.timestamp()).isNotNull();
            
            // Verify chronological ordering (Requirement 4.1)
            if (previousTimestamp != null) {
                // Messages created later should have timestamps that are equal or after previous ones
                // (equal is allowed since LocalDateTime.now() might return same value for rapid calls)
                assertThat(message.timestamp())
                    .isAfterOrEqualTo(previousTimestamp);
            }
            
            previousTimestamp = message.timestamp();
            
            // Add small delay to help ensure timestamp progression
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        
        // Verify the complete list maintains chronological order
        for (int i = 1; i < messages.size(); i++) {
            LocalDateTime current = messages.get(i).timestamp();
            LocalDateTime previous = messages.get(i - 1).timestamp();
            
            assertThat(current)
                .withFailMessage("Message at index %d has timestamp %s which is before previous message timestamp %s", 
                    i, current, previous)
                .isAfterOrEqualTo(previous);
        }
    }

    /**
     * Property: All factory methods produce messages with non-null timestamps
     * This ensures Requirements 4.7 is satisfied for all message creation methods.
     */
    @Property(tries = 100)
    void allFactoryMethodsProduceNonNullTimestamps(
        @ForAll @StringLength(min = 1, max = 100) String content,
        @ForAll @IntRange(min = 1, max = 10000) long generationTime
    ) {
        ChatMessage userMessage = ChatMessage.user(content);
        ChatMessage assistantMessage = ChatMessage.assistant(content, generationTime);
        ChatMessage systemMessage = ChatMessage.system(content);
        
        assertThat(userMessage.timestamp()).isNotNull();
        assertThat(assistantMessage.timestamp()).isNotNull();
        assertThat(systemMessage.timestamp()).isNotNull();
        
        // Verify the messages have the correct roles
        assertThat(userMessage.role()).isEqualTo("user");
        assertThat(assistantMessage.role()).isEqualTo("assistant");
        assertThat(systemMessage.role()).isEqualTo("system");
        
        // Verify content is preserved
        assertThat(userMessage.content()).isEqualTo(content);
        assertThat(assistantMessage.content()).isEqualTo(content);
        assertThat(systemMessage.content()).isEqualTo(content);
        
        // Verify generation time is set correctly
        assertThat(assistantMessage.generationTimeMs()).isEqualTo(generationTime);
        assertThat(userMessage.generationTimeMs()).isNull();
        assertThat(systemMessage.generationTimeMs()).isNull();
    }

    @Provide
    Arbitrary<List<String>> messageContents() {
        return Arbitraries.of(
            "Hello world",
            "How are you?",
            "Tell me a joke",
            "What is the weather like?",
            "Explain quantum physics",
            "Write a poem",
            "Help me with coding",
            "What's 2+2?",
            "Good morning",
            "Thank you"
        ).list().ofMinSize(1).ofMaxSize(50);
    }
}