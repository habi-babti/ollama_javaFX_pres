package com.ollama.olama.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for OllamaModel display format and validation.
 */
@Tag("Feature: ollama-chat, Property 2: Model Display Format")
class OllamaModelPropertyTest {

    /**
     * Property 2: Model Display Format
     * For any OllamaModel with a name and size, the display string (toString) 
     * SHALL contain both the model name and the human-readable size.
     * 
     * Validates: Requirements 1.3
     */
    @Property(tries = 100)
    void modelDisplayFormatContainsNameAndSize(
        @ForAll("modelNames") String name,
        @ForAll("humanReadableSizes") String size,
        @ForAll("byteSizes") long sizeBytes,
        @ForAll("modificationDates") String modifiedAt
    ) {
        // Create OllamaModel with the generated values
        OllamaModel model = new OllamaModel(name, size, sizeBytes, modifiedAt);
        
        // Get the display string
        String displayString = model.toString();
        
        // Verify the display string contains both name and size
        assertThat(displayString)
            .withFailMessage("Display string '%s' should contain model name '%s'", displayString, name)
            .contains(name);
            
        assertThat(displayString)
            .withFailMessage("Display string '%s' should contain size '%s'", displayString, size)
            .contains(size);
            
        // Verify the exact format: "name (size)"
        String expectedFormat = name + " (" + size + ")";
        assertThat(displayString)
            .withFailMessage("Display string should be in format 'name (size)' but was '%s'", displayString)
            .isEqualTo(expectedFormat);
    }

    @Provide
    Arbitrary<String> modelNames() {
        return Arbitraries.of(
            "llama2:latest",
            "codellama:7b",
            "mistral:7b-instruct",
            "phi:latest",
            "gemma:2b",
            "qwen:4b",
            "llama3:8b",
            "vicuna:13b",
            "alpaca:7b",
            "orca-mini:3b"
        );
    }

    @Provide
    Arbitrary<String> humanReadableSizes() {
        return Arbitraries.of(
            "3.8 GB",
            "7.4 GB",
            "13.0 GB",
            "1.2 GB",
            "4.1 GB",
            "2.7 GB",
            "8.5 GB",
            "15.2 GB",
            "900.5 MB",
            "1.8 TB"
        );
    }

    @Provide
    Arbitrary<Long> byteSizes() {
        return Arbitraries.longs()
            .between(1024L, 50_000_000_000L); // 1KB to ~50GB
    }

    @Provide
    Arbitrary<String> modificationDates() {
        return Arbitraries.of(
            "2024-01-15T10:30:00Z",
            "2024-02-20T14:45:30Z",
            "2024-03-10T09:15:45Z",
            "2024-04-05T16:20:10Z",
            "2024-05-12T11:35:25Z"
        );
    }
}