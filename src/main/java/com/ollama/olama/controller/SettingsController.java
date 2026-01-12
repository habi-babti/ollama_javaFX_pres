package com.ollama.olama.controller;

import com.ollama.olama.manager.SettingsManager;
import com.ollama.olama.manager.ThemeManager;
import com.ollama.olama.model.AppSettings;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the settings dialog.
 */
public class SettingsController {
    
    @FXML private TextField ollamaUrlField;
    @FXML private TextArea systemPromptArea;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> fontFamilyComboBox;
    @FXML private Spinner<Integer> fontSizeSpinner;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;
    
    private SettingsManager settingsManager;
    private AppSettings currentSettings;
    private Stage dialogStage;
    
    public void initialize() {
        // Set up form validation
        saveButton.disableProperty().bind(
            ollamaUrlField.textProperty().isEmpty()
        );
        
        // Set default system prompt placeholder
        systemPromptArea.setPromptText("Enter a system prompt to customize the AI's behavior (optional)");
        
        // Initialize appearance controls
        initializeAppearanceControls();
    }
    
    private void initializeAppearanceControls() {
        // Theme options
        themeComboBox.getItems().addAll("Light", "Dark", "Blue");
        themeComboBox.setValue("Light");
        
        // Font family options
        fontFamilyComboBox.getItems().addAll(
            "System", "Arial", "Helvetica", "Times New Roman", 
            "Courier New", "Verdana", "Georgia", "Comic Sans MS"
        );
        fontFamilyComboBox.setValue("System");
        
        // Font size spinner
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 24, 14));
        fontSizeSpinner.setEditable(true);
        
        // Add change listeners for live preview
        themeComboBox.setOnAction(e -> applyThemePreview());
        fontFamilyComboBox.setOnAction(e -> applyFontPreview());
        fontSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> applyFontPreview());
    }
    
    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        loadCurrentSettings();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    private void loadCurrentSettings() {
        if (settingsManager != null) {
            currentSettings = settingsManager.loadSettings();
            
            // Populate form fields
            ollamaUrlField.setText(currentSettings.ollamaBaseUrl());
            systemPromptArea.setText(currentSettings.systemPrompt());
            
            // Populate appearance settings
            String theme = currentSettings.theme();
            themeComboBox.setValue(capitalizeFirst(theme));
            fontFamilyComboBox.setValue(currentSettings.fontFamily());
            fontSizeSpinner.getValueFactory().setValue(currentSettings.fontSize());
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return "Light";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    @FXML
    private void handleSave() {
        try {
            // Create updated settings
            AppSettings updatedSettings = new AppSettings(
                ollamaUrlField.getText().trim(),
                currentSettings.windowWidth(),
                currentSettings.windowHeight(),
                currentSettings.windowX(),
                currentSettings.windowY(),
                currentSettings.lastSelectedModel(),
                systemPromptArea.getText().trim(),
                themeComboBox.getValue().toLowerCase(),
                fontFamilyComboBox.getValue(),
                (int) fontSizeSpinner.getValue()
            );
            
            // Save settings
            settingsManager.saveSettings(updatedSettings);
            
            // Apply theme to main window if it exists
            refreshMainWindowTheme(updatedSettings);
            
            showStatus("Settings saved successfully!", false);
            
            // Close dialog after a short delay
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (dialogStage != null) {
                    dialogStage.close();
                }
            }));
            timeline.play();
            
        } catch (IOException e) {
            showStatus("Failed to save settings: " + e.getMessage(), true);
        }
    }
    
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleReset() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Reset Settings");
        confirmDialog.setHeaderText("Reset to Default Settings");
        confirmDialog.setContentText("Are you sure you want to reset all settings to their default values?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Load default settings
                AppSettings defaultSettings = AppSettings.defaults();
                
                // Update form fields
                ollamaUrlField.setText(defaultSettings.ollamaBaseUrl());
                systemPromptArea.setText(defaultSettings.systemPrompt());
                themeComboBox.setValue(capitalizeFirst(defaultSettings.theme()));
                fontFamilyComboBox.setValue(defaultSettings.fontFamily());
                fontSizeSpinner.getValueFactory().setValue(defaultSettings.fontSize());
                
                showStatus("Settings reset to defaults", false);
            }
        });
    }
    
    @FXML
    private void handleTestConnection() {
        String url = ollamaUrlField.getText().trim();
        if (url.isEmpty()) {
            showStatus("Please enter an Ollama URL first", true);
            return;
        }
        
        // Disable button during test
        Button testButton = (Button) statusLabel.getScene().lookup("#testConnectionButton");
        if (testButton != null) {
            testButton.setDisable(true);
            testButton.setText("Testing...");
        }
        
        // Test connection in background thread
        Thread testThread = new Thread(() -> {
            try {
                // Simple HTTP test - you can enhance this with actual Ollama API call
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url + "/api/tags"))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
                
                java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
                
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        showStatus("✓ Connection successful!", false);
                    } else {
                        showStatus("✗ Connection failed (HTTP " + response.statusCode() + ")", true);
                    }
                    
                    // Re-enable button
                    if (testButton != null) {
                        testButton.setDisable(false);
                        testButton.setText("Test Connection");
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("✗ Connection failed: " + e.getMessage(), true);
                    
                    // Re-enable button
                    if (testButton != null) {
                        testButton.setDisable(false);
                        testButton.setText("Test Connection");
                    }
                });
            }
        });
        
        testThread.setDaemon(true);
        testThread.start();
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #2e7d32;");
        
        // Clear status after 5 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> statusLabel.setText("")));
        timeline.play();
    }
    
    private void applyThemePreview() {
        if (dialogStage != null && dialogStage.getScene() != null) {
            String selectedTheme = themeComboBox.getValue().toLowerCase();
            String themeFile = "/com/ollama/olama/themes/" + selectedTheme + "-theme.css";
            
            // Remove existing theme stylesheets
            dialogStage.getScene().getStylesheets().removeIf(stylesheet -> 
                stylesheet.contains("themes/"));
            
            // Add new theme
            try {
                String themeUrl = getClass().getResource(themeFile).toExternalForm();
                dialogStage.getScene().getStylesheets().add(themeUrl);
            } catch (Exception e) {
                // Fallback to base styles if theme not found
                System.err.println("Theme not found: " + themeFile);
            }
        }
    }
    
    private void applyFontPreview() {
        if (dialogStage != null && dialogStage.getScene() != null) {
            String fontFamily = fontFamilyComboBox.getValue();
            int fontSize = fontSizeSpinner.getValue();
            
            // Apply font settings to the dialog
            String fontStyle = String.format(
                "-fx-font-family: '%s'; -fx-font-size: %dpx;",
                fontFamily.equals("System") ? "System" : fontFamily,
                fontSize
            );
            
            dialogStage.getScene().getRoot().setStyle(fontStyle);
        }
    }
    
    private void refreshMainWindowTheme(AppSettings settings) {
        // Find the main window and refresh its theme
        if (dialogStage != null && dialogStage.getOwner() != null) {
            javafx.stage.Window owner = dialogStage.getOwner();
            if (owner instanceof javafx.stage.Stage) {
                javafx.stage.Stage mainStage = (javafx.stage.Stage) owner;
                if (mainStage.getScene() != null) {
                    ThemeManager.applyTheme(
                        mainStage.getScene(),
                        settings.theme(),
                        settings.fontFamily(),
                        settings.fontSize()
                    );
                }
            }
        }
    }
}