package com.ollama.olama.controller;

import com.ollama.olama.manager.ConversationManager;
import com.ollama.olama.manager.SettingsManager;
import com.ollama.olama.manager.ThemeManager;
import com.ollama.olama.model.AppSettings;
import com.ollama.olama.model.ChatMessage;
import com.ollama.olama.model.LoginSession;
import com.ollama.olama.model.OllamaModel;
import com.ollama.olama.service.AuthenticationService;
import com.ollama.olama.service.OllamaService;
import com.ollama.olama.ui.MessageBubble;
import com.ollama.olama.util.MessageValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main controller for the chat interface.
 * Handles UI events and coordinates between services for chat functionality.
 */
public class ChatController implements Initializable {
    
    // FXML injected components
    @FXML private ComboBox<OllamaModel> modelSelector;
    @FXML private VBox chatHistory;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    @FXML private Button cancelButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Button userManagementButton;
    @FXML private Label connectionStatus;
    @FXML private Label userInfoLabel;
    
    // Services - will be injected via constructor or setter
    private OllamaService ollamaService;
    private ConversationManager conversationManager;
    private SettingsManager settingsManager;
    private AuthenticationService authenticationService;
    
    // Authentication
    private LoginSession currentSession;
    
    // State
    private boolean isGenerating = false;
    private MessageBubble currentAssistantBubble;
    
    /**
     * Default constructor for FXML loading
     */
    public ChatController() {
        // Empty constructor for FXML
    }
    
    /**
     * Sets the required services for this controller
     */
    public void setServices(OllamaService ollamaService, 
                           ConversationManager conversationManager,
                           SettingsManager settingsManager,
                           AuthenticationService authenticationService) {
        this.ollamaService = ollamaService;
        this.conversationManager = conversationManager;
        this.settingsManager = settingsManager;
        this.authenticationService = authenticationService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI components
        setupModelSelector();
        setupMessageInput();
        setupConnectionStatus();
        
        // Load models and check connection when services are available
        Platform.runLater(this::initializeAfterServicesSet);
    }
    
    /**
     * Called after services are set to complete initialization
     */
    private void initializeAfterServicesSet() {
        if (ollamaService != null) {
            loadAvailableModels();
            checkConnectionStatus();
        }
    }
    
    private void setupModelSelector() {
        if (modelSelector != null) {
            modelSelector.setOnAction(e -> onModelSelected());
            modelSelector.setPromptText("Select a model...");
        }
    }
    
    private void setupMessageInput() {
        if (messageInput != null) {
            messageInput.setOnKeyPressed(this::onInputKeyPressed);
            messageInput.setPromptText("Type your message here... (Shift+Enter for new line, Enter to send)");
            messageInput.setWrapText(true);
        }
    }
    
    private void setupConnectionStatus() {
        if (connectionStatus != null) {
            connectionStatus.setText("● Disconnected");
            connectionStatus.getStyleClass().add("connection-status-disconnected");
        }
    }
    
    private void loadAvailableModels() {
        if (ollamaService == null) return;
        
        // Clear existing models
        modelSelector.getItems().clear();
        
        // Load models in background thread
        Thread modelThread = new Thread(() -> {
            try {
                ollamaService.getAvailableModels().thenAccept(models -> {
                    Platform.runLater(() -> {
                        modelSelector.getItems().addAll(models);
                        if (!models.isEmpty()) {
                            // Select first model by default
                            modelSelector.getSelectionModel().selectFirst();
                        }
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showError("Failed to load models: " + throwable.getMessage());
                    });
                    return null;
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to load models: " + e.getMessage());
                });
            }
        });
        modelThread.setDaemon(true);
        modelThread.start();
    }
    
    private void checkConnectionStatus() {
        if (ollamaService == null) return;
        
        // Update status to connecting
        connectionStatus.setText("● Connecting...");
        connectionStatus.getStyleClass().clear();
        connectionStatus.getStyleClass().add("connection-status-connecting");
        
        // Check connection in background thread
        Thread connectionThread = new Thread(() -> {
            try {
                ollamaService.checkConnection().thenAccept(connected -> {
                    Platform.runLater(() -> {
                        if (connected) {
                            connectionStatus.setText("● Connected");
                            connectionStatus.getStyleClass().clear();
                            connectionStatus.getStyleClass().add("connection-status-connected");
                        } else {
                            connectionStatus.setText("● Disconnected");
                            connectionStatus.getStyleClass().clear();
                            connectionStatus.getStyleClass().add("connection-status-disconnected");
                        }
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        connectionStatus.setText("● Error");
                        connectionStatus.getStyleClass().clear();
                        connectionStatus.getStyleClass().add("connection-status-disconnected");
                    });
                    return null;
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectionStatus.setText("● Error");
                    connectionStatus.getStyleClass().clear();
                    connectionStatus.getStyleClass().add("connection-status-disconnected");
                });
            }
        });
        connectionThread.setDaemon(true);
        connectionThread.start();
    }
    
    /**
     * Adds a message bubble to the chat history with proper alignment
     */
    private void addMessageBubble(MessageBubble bubble) {
        // Create a container for proper alignment
        HBox container = new HBox();
        container.setPrefWidth(Region.USE_COMPUTED_SIZE);
        container.setMaxWidth(Double.MAX_VALUE);
        
        // Set alignment based on message role
        if (bubble.getRole() == MessageBubble.Role.USER) {
            // User messages on the right
            container.setAlignment(Pos.CENTER_RIGHT);
            HBox.setMargin(bubble, new Insets(0, 0, 0, 50)); // Left margin to limit width
        } else if (bubble.getRole() == MessageBubble.Role.ASSISTANT) {
            // Assistant messages on the left
            container.setAlignment(Pos.CENTER_LEFT);
            HBox.setMargin(bubble, new Insets(0, 50, 0, 0)); // Right margin to limit width
        } else {
            // System and error messages centered
            container.setAlignment(Pos.CENTER);
            HBox.setMargin(bubble, new Insets(0, 50, 0, 50)); // Both margins
        }
        
        container.getChildren().add(bubble);
        chatHistory.getChildren().add(container);
    }
    
    private void showError(String message) {
        MessageBubble errorBubble = MessageBubble.error(message);
        addMessageBubble(errorBubble);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }
    
    @FXML
    private void onSendMessage() {
        String messageText = messageInput.getText().trim();
        
        // Validate message
        var validationResult = MessageValidator.isValidMessage(messageText);
        if (!validationResult.isValid()) {
            showError(validationResult.errorMessage());
            return;
        }
        
        // Check if model is selected
        OllamaModel selectedModel = modelSelector.getSelectionModel().getSelectedItem();
        if (selectedModel == null) {
            showError("Please select a model first");
            return;
        }
        
        // Create user message
        ChatMessage userMessage = ChatMessage.user(messageText);
        
        // Add user message to conversation and UI
        conversationManager.addMessage(userMessage);
        MessageBubble userBubble = new MessageBubble(userMessage);
        addMessageBubble(userBubble);
        
        // Clear input and set UI state for generation
        messageInput.clear();
        setGeneratingState(true);
        
        // Create assistant message bubble for streaming
        currentAssistantBubble = new MessageBubble(
            MessageBubble.Role.ASSISTANT, 
            "", 
            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        );
        currentAssistantBubble.setTyping(true);
        addMessageBubble(currentAssistantBubble);
        scrollToBottom();
        
        // Send message to Ollama in background
        long startTime = System.currentTimeMillis();
        ollamaService.sendChatMessage(
            selectedModel.name(),
            conversationManager.getMessagesForApi(),
            this::onTokenReceived
        ).thenAccept(assistantMessage -> {
            long generationTime = System.currentTimeMillis() - startTime;
            
            Platform.runLater(() -> {
                // Create final message with generation time
                ChatMessage finalMessage = ChatMessage.assistant(
                    assistantMessage.content(), 
                    generationTime
                );
                
                // Add to conversation
                conversationManager.addMessage(finalMessage);
                
                // Update timestamp to show generation time
                String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                ) + " (" + generationTime + "ms)";
                currentAssistantBubble.updateTimestamp(timestamp);
                
                // Reset UI state
                setGeneratingState(false);
                currentAssistantBubble = null;
                
                // Return focus to input
                messageInput.requestFocus();
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                // Handle error
                currentAssistantBubble.setTyping(false);
                currentAssistantBubble.setContent("Error: " + throwable.getMessage());
                currentAssistantBubble.getStyleClass().add("message-bubble-error");
                
                setGeneratingState(false);
                currentAssistantBubble = null;
                messageInput.requestFocus();
            });
            return null;
        });
    }
    
    /**
     * Called when a token is received during streaming
     */
    private void onTokenReceived(String token) {
        Platform.runLater(() -> {
            if (currentAssistantBubble != null) {
                currentAssistantBubble.setTyping(false);
                currentAssistantBubble.appendText(token);
                scrollToBottom();
            }
        });
    }
    
    @FXML
    private void onClearChat() {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Clear Chat");
        confirmDialog.setHeaderText("Clear Conversation");
        confirmDialog.setContentText("Are you sure you want to clear the current conversation? This action cannot be undone.");
        
        // Add custom buttons
        ButtonType clearButton = new ButtonType("Clear");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(clearButton, cancelButton);
        
        // Show dialog and handle response
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == clearButton) {
                clearConversation();
            }
        });
    }
    
    /**
     * Clears the conversation and UI
     */
    private void clearConversation() {
        // Clear conversation manager
        if (conversationManager != null) {
            conversationManager.clearConversation();
        }
        
        // Clear UI
        chatHistory.getChildren().clear();
        
        // Reset state
        if (isGenerating) {
            setGeneratingState(false);
        }
        currentAssistantBubble = null;
        
        // Return focus to input
        messageInput.requestFocus();
    }
    
    @FXML
    private void onRefreshModels() {
        loadAvailableModels();
        checkConnectionStatus();
    }
    
    @FXML
    private void onCancelGeneration() {
        if (isGenerating && ollamaService != null) {
            ollamaService.cancelCurrentRequest();
            
            // Update UI state
            if (currentAssistantBubble != null) {
                currentAssistantBubble.setTyping(false);
                currentAssistantBubble.setContent("Generation cancelled");
                currentAssistantBubble.getStyleClass().add("message-bubble-error");
                currentAssistantBubble = null;
            }
            
            setGeneratingState(false);
            messageInput.requestFocus();
        }
    }
    
    /**
     * Sets the UI state for generation mode
     */
    private void setGeneratingState(boolean generating) {
        this.isGenerating = generating;
        
        // Disable/enable input controls
        messageInput.setDisable(generating);
        sendButton.setDisable(generating);
        modelSelector.setDisable(generating);
        clearButton.setDisable(generating);
        refreshButton.setDisable(generating);
        
        // Show/hide cancel button
        cancelButton.setVisible(generating);
        cancelButton.setManaged(generating);
        
        // Update send button text
        if (generating) {
            sendButton.setText("Generating...");
        } else {
            sendButton.setText("Send ➤");
        }
    }
    
    @FXML
    private void onShowSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ollama/olama/settings-view.fxml"));
            Parent root = loader.load();
            
            SettingsController controller = loader.getController();
            controller.setSettingsManager(settingsManager);
            
            Stage stage = new Stage();
            stage.setTitle("Settings");
            Scene scene = new Scene(root, 600, 500);
            
            // Apply current theme to the settings window
            if (settingsManager != null) {
                AppSettings settings = settingsManager.loadSettings();
                ThemeManager.applyTheme(scene, settings.theme(), settings.fontFamily(), settings.fontSize());
            }
            
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(messageInput.getScene().getWindow());
            stage.setResizable(false);
            
            controller.setDialogStage(stage);
            
            stage.show();
            
        } catch (IOException e) {
            showError("Failed to open settings: " + e.getMessage());
        }
    }
    
    @FXML
    private void onInputKeyPressed(KeyEvent event) {
        // Handle Enter key for sending messages
        if (event.getCode() == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                // Shift+Enter: Insert newline (default behavior, do nothing)
                return;
            } else {
                // Enter: Send message
                event.consume(); // Prevent default newline insertion
                if (!isGenerating) {
                    onSendMessage();
                }
            }
        }
        // Handle Escape key for canceling generation
        else if (event.getCode() == KeyCode.ESCAPE) {
            if (isGenerating) {
                onCancelGeneration();
            }
        }
        // Handle Ctrl+N for new conversation
        else if (event.getCode() == KeyCode.N && event.isControlDown()) {
            event.consume();
            onClearChat();
        }
        // Handle Ctrl+R for refresh models
        else if (event.getCode() == KeyCode.R && event.isControlDown()) {
            event.consume();
            onRefreshModels();
        }
    }
    
    /**
     * Sets up global keyboard shortcuts for the scene
     * This should be called after the scene is set
     */
    public void setupGlobalKeyboardShortcuts() {
        // Get the scene from one of the FXML components
        if (messageInput.getScene() != null) {
            messageInput.getScene().setOnKeyPressed(event -> {
                // Handle global shortcuts when not in text input
                if (!messageInput.isFocused()) {
                    // Ctrl+N: New conversation
                    if (event.getCode() == KeyCode.N && event.isControlDown()) {
                        event.consume();
                        onClearChat();
                    }
                    // Ctrl+R: Refresh models
                    else if (event.getCode() == KeyCode.R && event.isControlDown()) {
                        event.consume();
                        onRefreshModels();
                    }
                    // Escape: Cancel generation
                    else if (event.getCode() == KeyCode.ESCAPE && isGenerating) {
                        event.consume();
                        onCancelGeneration();
                    }
                }
            });
        }
    }
    
    private void onModelSelected() {
        OllamaModel selectedModel = modelSelector.getSelectionModel().getSelectedItem();
        if (selectedModel != null) {
            // Show model change notification
            MessageBubble systemBubble = new MessageBubble(
                MessageBubble.Role.SYSTEM, 
                "Model changed to: " + selectedModel.name(),
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            );
            addMessageBubble(systemBubble);
            scrollToBottom();
        }
    }
    
    // Authentication-related methods
    
    /**
     * Sets the current login session and updates UI accordingly
     */
    public void setLoginSession(LoginSession session) {
        this.currentSession = session;
        updateUserInterface();
    }
    
    /**
     * Updates the UI based on current user session
     */
    private void updateUserInterface() {
        if (currentSession != null) {
            // Update user info label
            if (userInfoLabel != null) {
                userInfoLabel.setText("Logged in as: " + currentSession.getUsername() + 
                    " (" + currentSession.getRole().name().toLowerCase() + ")");
            }
            
            // Show/hide admin features
            if (userManagementButton != null) {
                userManagementButton.setVisible(currentSession.isAdmin());
                userManagementButton.setManaged(currentSession.isAdmin());
            }
        }
    }
    
    @FXML
    private void onLogout() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Logout");
        confirmDialog.setHeaderText("Confirm Logout");
        confirmDialog.setContentText("Are you sure you want to logout?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear current session
                currentSession = null;
                
                // Return to login screen
                returnToLogin();
            }
        });
    }
    
    @FXML
    private void onUserManagement() {
        if (currentSession == null || !currentSession.isAdmin()) {
            showError("Access denied. Admin privileges required.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ollama/olama/user-management-view.fxml"));
            Parent root = loader.load();
            
            UserManagementController controller = loader.getController();
            controller.setAuthenticationService(authenticationService);
            controller.setCurrentSession(currentSession);
            
            Stage stage = new Stage();
            stage.setTitle("User Management");
            Scene scene = new Scene(root, 800, 600);
            
            // Apply current theme to the user management window
            if (settingsManager != null) {
                AppSettings settings = settingsManager.loadSettings();
                ThemeManager.applyTheme(scene, settings.theme(), settings.fontFamily(), settings.fontSize());
            }
            
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(messageInput.getScene().getWindow());
            
            stage.show();
            
        } catch (IOException e) {
            showError("Failed to open user management: " + e.getMessage());
        }
    }
    
    /**
     * Returns to the login screen
     */
    private void returnToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ollama/olama/login-view.fxml"));
            Parent root = loader.load();
            
            LoginController loginController = loader.getController();
            loginController.setAuthenticationService(authenticationService);
            loginController.setSettingsManager(settingsManager);
            
            Stage stage = (Stage) messageInput.getScene().getWindow();
            loginController.setPrimaryStage(stage);
            
            Scene scene = new Scene(root, 400, 500);
            
            // Apply current theme to the login screen
            if (settingsManager != null) {
                AppSettings settings = settingsManager.loadSettings();
                ThemeManager.applyTheme(scene, settings.theme(), settings.fontFamily(), settings.fontSize());
            } else {
                // Fallback to base styles if no settings manager
                scene.getStylesheets().add(getClass().getResource("/com/ollama/olama/styles.css").toExternalForm());
            }
            
            stage.setScene(scene);
            stage.setTitle("Ollama Chat - Login");
            
        } catch (IOException e) {
            showError("Failed to return to login: " + e.getMessage());
        }
    }
    
    /**
     * Refreshes the application theme based on current settings
     */
    public void refreshTheme() {
        if (settingsManager != null && messageInput.getScene() != null) {
            AppSettings settings = settingsManager.loadSettings();
            ThemeManager.applyTheme(
                messageInput.getScene(), 
                settings.theme(), 
                settings.fontFamily(), 
                settings.fontSize()
            );
        }
    }
}