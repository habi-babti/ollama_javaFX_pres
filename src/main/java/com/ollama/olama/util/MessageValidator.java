package com.ollama.olama.util;

/**
 * Utility class for validating chat messages before submission.
 * Ensures messages meet the requirements for processing by the chat interface.
 */
public class MessageValidator {
    
    /**
     * Result of message validation containing the validation status and error message if invalid.
     */
    public record ValidationResult(boolean isValid, String errorMessage) {
        
        /**
         * Creates a successful validation result.
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        /**
         * Creates a failed validation result with an error message.
         */
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }
    
    /**
     * Validates a message for submission to the chat interface.
     * 
     * @param message the message content to validate
     * @return ValidationResult indicating if the message is valid and any error message
     */
    public static ValidationResult isValidMessage(String message) {
        if (message == null) {
            return ValidationResult.invalid("Message cannot be null");
        }
        
        if (message.trim().isEmpty()) {
            return ValidationResult.invalid("Message cannot be empty or contain only whitespace");
        }
        
        return ValidationResult.valid();
    }
}