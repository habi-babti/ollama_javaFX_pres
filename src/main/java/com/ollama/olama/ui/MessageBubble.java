package com.ollama.olama.ui;

import com.ollama.olama.model.ChatMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Custom JavaFX component for displaying chat messages with role-based styling.
 * Extends HBox to provide a styled message bubble with content and timestamp.
 */
public class MessageBubble extends HBox {
    
    public enum Role { 
        USER, ASSISTANT, SYSTEM, ERROR 
    }
    
    private final Label contentLabel;
    private final Label timestampLabel;
    private final Role role;
    private final VBox messageContainer;
    private boolean isTyping = false;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Creates a MessageBubble from a ChatMessage
     */
    public MessageBubble(ChatMessage message) {
        this(Role.valueOf(message.role().toUpperCase()), message.content(), 
             message.timestamp().format(TIME_FORMATTER));
    }
    
    /**
     * Creates a MessageBubble with specified role, content, and timestamp
     */
    public MessageBubble(Role role, String content, String timestamp) {
        this.role = role;
        
        // Create content label
        this.contentLabel = new Label(content);
        this.contentLabel.setWrapText(true);
        this.contentLabel.setMaxWidth(400); // Limit bubble width
        
        // Create timestamp label
        this.timestampLabel = new Label(timestamp);
        this.timestampLabel.getStyleClass().add("message-timestamp");
        
        // Create container for message content and timestamp
        this.messageContainer = new VBox(5);
        this.messageContainer.getChildren().addAll(contentLabel, timestampLabel);
        
        // Configure the HBox layout
        this.getChildren().add(messageContainer);
        this.setPadding(new Insets(8, 12, 8, 12));
        this.setSpacing(10);
        
        // Apply role-based styling and alignment
        applyRoleStyles();
    }
    
    /**
     * Creates an error message bubble
     */
    public static MessageBubble error(String errorMessage) {
        return new MessageBubble(Role.ERROR, errorMessage, 
                               java.time.LocalTime.now().format(TIME_FORMATTER));
    }
    
    /**
     * Appends text to the content label for streaming responses
     */
    public void appendText(String text) {
        if (text != null && !text.isEmpty()) {
            String currentText = contentLabel.getText();
            contentLabel.setText(currentText + text);
        }
    }
    
    /**
     * Sets the complete message content, replacing any existing content
     */
    public void setContent(String content) {
        if (content != null) {
            contentLabel.setText(content);
        }
    }
    
    /**
     * Shows or hides typing indicator for assistant messages
     */
    public void setTyping(boolean typing) {
        this.isTyping = typing;
        if (typing && role == Role.ASSISTANT) {
            contentLabel.setText("● ● ● typing...");
            contentLabel.getStyleClass().add("typing-indicator");
        } else {
            contentLabel.getStyleClass().remove("typing-indicator");
            // Clear the typing text when stopping typing
            if (!typing && contentLabel.getText().equals("● ● ● typing...")) {
                contentLabel.setText("");
            }
        }
    }
    
    /**
     * Gets the current content text
     */
    public String getContent() {
        return contentLabel.getText();
    }
    
    /**
     * Gets the message role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Applies CSS styling and alignment based on message role
     */
    private void applyRoleStyles() {
        // Clear any existing style classes
        this.getStyleClass().clear();
        
        // Add base message bubble class
        this.getStyleClass().add("message-bubble");
        
        switch (role) {
            case USER:
                this.getStyleClass().add("message-bubble-user");
                // For user messages, we want the entire bubble aligned to the right
                this.setAlignment(Pos.CENTER_LEFT); // Content within bubble
                break;
            case ASSISTANT:
                this.getStyleClass().add("message-bubble-assistant");
                // For assistant messages, we want the entire bubble aligned to the left
                this.setAlignment(Pos.CENTER_LEFT); // Content within bubble
                break;
            case SYSTEM:
                this.getStyleClass().add("message-bubble-system");
                this.setAlignment(Pos.CENTER);
                break;
            case ERROR:
                this.getStyleClass().add("message-bubble-error");
                this.setAlignment(Pos.CENTER_LEFT);
                break;
        }
    }
    
    /**
     * Updates the timestamp display
     */
    public void updateTimestamp(String timestamp) {
        timestampLabel.setText(timestamp);
    }
}