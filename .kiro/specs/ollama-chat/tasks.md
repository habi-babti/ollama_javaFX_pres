# Implementation Plan: Ollama Chat Application

## Overview

This plan implements a JavaFX desktop application for chatting with local LLM models via Ollama. Tasks are organized to build incrementally, starting with data models, then services, and finally the UI layer.

## Tasks

- [x] 1. Project setup and dependencies
  - Add jqwik, mockito, and jackson dependencies to pom.xml
  - Update module-info.java with required modules (java.net.http, com.fasterxml.jackson)
  - Create package structure: model, service, manager, controller, ui
  - _Requirements: Project infrastructure_

- [x] 2. Implement data models
  - [x] 2.1 Create ChatMessage record
    - Implement record with role, content, timestamp, generationTimeMs fields
    - Add factory methods: user(), assistant(), system()
    - Add toApiFormat() method for Ollama API
    - _Requirements: 2.4, 3.1, 4.1, 4.7_
  - [x] 2.2 Write property test for ChatMessage timestamp

    - **Property 4: Chronological Message Ordering with Timestamps**
    - **Validates: Requirements 4.1, 4.7**
  - [x] 2.3 Create OllamaModel record
    - Implement record with name, size, sizeBytes, modifiedAt fields
    - Add toString() returning "name (size)" format
    - Add fromJson() static factory method
    - _Requirements: 1.2, 1.3_
  - [x] 2.4 Write property test for OllamaModel display format

    - **Property 2: Model Display Format**
    - **Validates: Requirements 1.3**
  - [x] 2.5 Create AppSettings record
    - Implement record with ollamaBaseUrl, window dimensions, lastSelectedModel, systemPrompt
    - Add defaults() factory method
    - _Requirements: 5.7, 6.6_

- [x] 3. Checkpoint - Verify data models
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Implement ConversationManager
  - [ ] 4.1 Create ConversationManager interface and implementation
    - Implement addMessage(), getMessages(), clearConversation()
    - Implement getMessagesForApi() including system prompt
    - Implement setSystemPrompt()
    - _Requirements: 4.1, 4.3, 4.6, 9.2_
  - [ ]* 4.2 Write property test for message ordering
    - **Property 4: Chronological Message Ordering with Timestamps**
    - **Validates: Requirements 4.1, 4.7**
  - [ ]* 4.3 Write property test for clear conversation
    - **Property 6: Clear Conversation Resets State**
    - **Validates: Requirements 4.6**
  - [ ]* 4.4 Write property test for full context in API requests
    - **Property 5: Full Context in API Requests**
    - **Validates: Requirements 4.3**
  - [ ]* 4.5 Write property test for system prompt position
    - **Property 8: System Prompt First in API Requests**
    - **Validates: Requirements 9.2**
  - [x] 4.6 Implement conversation save/load to JSON file
    - Add saveToFile() and loadFromFile() methods
    - Use Jackson for JSON serialization
    - _Requirements: 8.1, 8.3_
  - [ ]* 4.7 Write property test for serialization round-trip
    - **Property 7: Conversation Serialization Round-Trip**
    - **Validates: Requirements 8.1, 8.3**

- [x] 5. Checkpoint - Verify ConversationManager
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement OllamaService
  - [x] 6.1 Create OllamaService interface
    - Define getAvailableModels(), sendChatMessage(), cancelCurrentRequest(), checkConnection(), setBaseUrl()
    - _Requirements: 1.1, 2.1, 5.1, 5.7_
  - [x] 6.2 Implement OllamaServiceImpl with HttpClient
    - Implement getAvailableModels() calling /api/tags
    - Parse JSON response into List<OllamaModel>
    - Sort models alphabetically by name
    - _Requirements: 1.1, 1.2_
  - [ ]* 6.3 Write property test for model sorting
    - **Property 1: Model List Alphabetical Sorting**
    - **Validates: Requirements 1.2**
  - [x] 6.4 Implement sendChatMessage() with streaming
    - Build POST request to /api/chat with stream:true
    - Parse streaming NDJSON response
    - Call onToken callback for each token
    - Return complete ChatMessage when done
    - _Requirements: 2.1, 3.1, 3.2_
  - [x] 6.5 Implement checkConnection() and setBaseUrl()
    - Check connectivity by calling /api/tags
    - Allow configuring custom base URL
    - _Requirements: 5.1, 5.2, 5.7_
  - [ ]* 6.6 Write property test for URL configuration
    - **Property 9: Custom URL Configuration**
    - **Validates: Requirements 5.7**
  - [x] 6.7 Implement error handling with OllamaException
    - Create OllamaException with Type enum
    - Handle connection errors, timeouts, API errors
    - _Requirements: 5.3, 5.4, 5.5_

- [x] 7. Checkpoint - Verify OllamaService
  - Ensure all tests pass, ask the user if questions arise.

- [-] 8. Implement input validation
  - [x] 8.1 Create MessageValidator utility class
    - Implement isValidMessage() checking for empty/whitespace
    - Return validation result with error message
    - _Requirements: 2.5_
  - [ ]* 8.2 Write property test for whitespace rejection
    - **Property 3: Whitespace Message Rejection**
    - **Validates: Requirements 2.5**

- [x] 9. Implement SettingsManager
  - [x] 9.1 Create SettingsManager for app settings persistence
    - Load/save AppSettings to JSON file in user home directory
    - Handle window position, size, last model, system prompt
    - _Requirements: 5.7, 6.6, 9.1, 9.3_

- [x] 10. Implement UI components
  - [x] 10.1 Create MessageBubble custom component
    - Extend HBox with styled message display
    - Support USER, ASSISTANT, SYSTEM, ERROR roles
    - Add appendText() for streaming, setTyping() for indicator
    - Style with CSS for different roles
    - _Requirements: 2.4, 3.1, 4.2_
  - [x] 10.2 Create main-view.fxml layout
    - Top toolbar: model ComboBox, refresh button, connection status
    - Center: ScrollPane with VBox for chat history
    - Bottom: TextArea for input, Clear button, Send button
    - _Requirements: 6.1, 6.2, 6.3_
  - [x] 10.3 Create application stylesheet (styles.css)
    - Define styles for message bubbles (user/assistant/error)
    - Style toolbar, input area, buttons
    - Add typing indicator animation
    - _Requirements: 6.4, 6.7, 6.8_

- [x] 11. Implement ChatController
  - [x] 11.1 Create ChatController with FXML bindings
    - Inject UI components and services
    - Initialize model selector with available models
    - Set up connection status indicator
    - _Requirements: 1.1, 1.4, 5.1_
  - [x] 11.2 Implement message sending logic
    - Handle Send button click and Enter key
    - Validate input before sending
    - Add user message to history, call OllamaService
    - Display streaming response in MessageBubble
    - _Requirements: 2.1, 2.2, 2.4, 2.5, 2.6, 3.1, 3.2_
  - [x] 11.3 Implement UI state management
    - Disable input during generation
    - Show typing indicator while waiting
    - Auto-scroll to newest message
    - _Requirements: 2.7, 3.3, 3.4_
  - [x] 11.4 Implement clear chat functionality
    - Show confirmation dialog
    - Clear conversation and UI
    - _Requirements: 4.5, 4.6_
  - [x] 11.5 Implement keyboard shortcuts
    - Ctrl+N: New conversation
    - Ctrl+R: Refresh models
    - Escape: Cancel generation
    - Shift+Enter: Newline in input
    - _Requirements: 2.3, 7.1, 7.2, 7.3_

- [x] 12. Implement main application
  - [x] 12.1 Update HelloApplication to OllamaChatApplication
    - Load main-view.fxml
    - Set window title, minimum size (800x600)
    - Apply stylesheet
    - _Requirements: 6.5_
  - [x] 12.2 Implement window state persistence
    - Save window size/position on close
    - Restore on startup
    - _Requirements: 6.6_
  - [x] 12.3 Wire up dependency injection
    - Create service instances
    - Pass to controller
    - _Requirements: Architecture_

- [x] 13. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.
  - Verify application runs and connects to Ollama
  - Test full chat flow with a local model

## Notes

- Tasks marked with `*` are optional property-based tests
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests use jqwik framework with minimum 100 iterations
- Unit tests complement property tests for edge cases
