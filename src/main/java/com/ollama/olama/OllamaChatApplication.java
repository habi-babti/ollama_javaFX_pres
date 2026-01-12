package com.ollama.olama;

import com.ollama.olama.controller.LoginController;
import com.ollama.olama.manager.SettingsManager;
import com.ollama.olama.manager.SettingsManagerImpl;
import com.ollama.olama.manager.ThemeManager;
import com.ollama.olama.model.AppSettings;
import com.ollama.olama.service.AuthenticationService;
import com.ollama.olama.service.AuthenticationServiceImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class OllamaChatApplication extends Application {
    private SettingsManager settingsManager;
    private AuthenticationService authenticationService;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize services
        settingsManager = new SettingsManagerImpl();
        authenticationService = new AuthenticationServiceImpl();
        
        // Load login screen
        FXMLLoader fxmlLoader = new FXMLLoader(OllamaChatApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);
        
        // Apply theme from settings to login screen
        AppSettings settings = settingsManager.loadSettings();
        ThemeManager.applyTheme(scene, settings.theme(), settings.fontFamily(), settings.fontSize());
        
        // Get the controller and inject dependencies
        LoginController controller = fxmlLoader.getController();
        controller.setAuthenticationService(authenticationService);
        controller.setSettingsManager(settingsManager);
        controller.setPrimaryStage(stage);
        
        stage.setTitle("Ollama Chat - Login");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}