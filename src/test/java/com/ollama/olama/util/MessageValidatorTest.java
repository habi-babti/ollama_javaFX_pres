package com.ollama.olama.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageValidator utility class.
 */
class MessageValidatorTest {
    
    @Test
    void testValidMessage() {
        var result = MessageValidator.isValidMessage("Hello, world!");
        assertTrue(result.isValid());
        assertNull(result.errorMessage());
    }
    
    @Test
    void testValidMessageWithSpaces() {
        var result = MessageValidator.isValidMessage("  Hello with spaces  ");
        assertTrue(result.isValid());
        assertNull(result.errorMessage());
    }
    
    @Test
    void testNullMessage() {
        var result = MessageValidator.isValidMessage(null);
        assertFalse(result.isValid());
        assertEquals("Message cannot be null", result.errorMessage());
    }
    
    @Test
    void testEmptyMessage() {
        var result = MessageValidator.isValidMessage("");
        assertFalse(result.isValid());
        assertEquals("Message cannot be empty or contain only whitespace", result.errorMessage());
    }
    
    @Test
    void testWhitespaceOnlyMessage() {
        var result = MessageValidator.isValidMessage("   ");
        assertFalse(result.isValid());
        assertEquals("Message cannot be empty or contain only whitespace", result.errorMessage());
    }
    
    @Test
    void testTabsAndNewlinesOnly() {
        var result = MessageValidator.isValidMessage("\t\n\r ");
        assertFalse(result.isValid());
        assertEquals("Message cannot be empty or contain only whitespace", result.errorMessage());
    }
    
    @Test
    void testValidationResultFactoryMethods() {
        var validResult = MessageValidator.ValidationResult.valid();
        assertTrue(validResult.isValid());
        assertNull(validResult.errorMessage());
        
        var invalidResult = MessageValidator.ValidationResult.invalid("Test error");
        assertFalse(invalidResult.isValid());
        assertEquals("Test error", invalidResult.errorMessage());
    }
}