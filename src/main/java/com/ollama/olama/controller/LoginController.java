package com.ollama.olama.controller;

import com.ollama.olama.manager.ConversationManager;
import com.ollama.olama.manager.ConversationManagerImpl;
import com.ollama.olama.manager.SettingsManager;
import com.ollama.olama.manager.SettingsManagerImpl;
import com.ollama.olama.manager.ThemeManager;
import com.ollama.olama.model.AppSettings;
import com.ollama.olama.model.LoginSession;
import com.ollama.olama.service.AuthenticationService;
import com.ollama.olama.service.OllamaService;
import com.ollama.olama.service.OllamaServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the login screen.
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    private AuthenticationService authService;
    private SettingsManager settingsManager;
    private Stage primaryStage;
    
    public void initialize() {
        // Set up enter key handling
        passwordField.setOnAction(e -> handleLogin());
        
        // Bind login button to form validity
        loginButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty())
                .or(loadingIndicator.visibleProperty())
        );
        
        // Hide loading indicator initially
        loadingIndicator.setVisible(false);
        
        // Focus username field
        Platform.runLater(() -> usernameField.requestFocus());
    }
    
    public void setAuthenticationService(AuthenticationService authService) {
        this.authService = authService;
    }
    
    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password", true);
            return;
        }
        
        // Show loading
        loadingIndicator.setVisible(true);
        statusLabel.setText("Authenticating...");
        
        // Authenticate in background thread
        Thread authThread = new Thread(() -> {
            try {
                Optional<LoginSession> session = authService.authenticate(username, password);
                
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    
                    if (session.isPresent()) {
                        openMainApplication(session.get());
                    } else {
                        showStatus("Invalid username or password", true);
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showStatus("Login failed: " + e.getMessage(), true);
                });
            }
        });
        
        authThread.setDaemon(true);
        authThread.start();
    }
    
    private void openMainApplication(LoginSession session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ollama/olama/main-view.fxml"));
            Parent root = loader.load();
            
            ChatController chatController = loader.getController();
            
            // Create all required services
            OllamaService ollamaService = new OllamaServiceImpl();
            ConversationManager conversationManager = new ConversationManagerImpl();
            SettingsManager settingsManager = new SettingsManagerImpl();
            
            // Set services and session
            chatController.setServices(ollamaService, conversationManager, settingsManager, authService);
            chatController.setLoginSession(session);
            
            Scene scene = new Scene(root, 1000, 700);
            
            // Apply theme from settings
            AppSettings settings = settingsManager.loadSettings();
            ThemeManager.applyTheme(scene, settings.theme(), settings.fontFamily(), settings.fontSize());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Ollama Chat - " + session.getUsername() + " (" + session.getRole().name().toLowerCase() + ")");
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
        } catch (IOException e) {
            showStatus("Failed to load main application: " + e.getMessage(), true);
        }
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #2e7d32;");
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
}