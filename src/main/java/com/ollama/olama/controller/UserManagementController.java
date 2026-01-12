package com.ollama.olama.controller;

import com.ollama.olama.model.LoginSession;
import com.ollama.olama.model.User;
import com.ollama.olama.service.AuthenticationException;
import com.ollama.olama.service.AuthenticationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for user management (admin only).
 */
public class UserManagementController {
    
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> createdColumn;
    @FXML private TableColumn<User, String> lastLoginColumn;
    
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<User.Role> newRoleCombo;
    @FXML private Button createUserButton;
    
    @FXML private TextField changePasswordUsernameField;
    @FXML private PasswordField newPasswordForUserField;
    @FXML private Button changePasswordButton;
    
    @FXML private Label statusLabel;
    
    private AuthenticationService authService;
    private LoginSession currentSession;
    private ObservableList<User> usersList;
    
    public void initialize() {
        setupTable();
        setupForm();
        usersList = FXCollections.observableArrayList();
        usersTable.setItems(usersList);
    }
    
    public void setAuthenticationService(AuthenticationService authService) {
        this.authService = authService;
        refreshUsersList();
    }
    
    public void setCurrentSession(LoginSession session) {
        this.currentSession = session;
    }
    
    private void setupTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().role().name()));
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
        createdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().createdAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        lastLoginColumn.setCellValueFactory(cellData -> {
            var lastLogin = cellData.getValue().lastLogin();
            return new SimpleStringProperty(lastLogin != null ? 
                lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never");
        });
        
        // Context menu for table
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem activateItem = new MenuItem("Activate User");
        activateItem.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toggleUserStatus(selected.username(), true);
            }
        });
        
        MenuItem deactivateItem = new MenuItem("Deactivate User");
        deactivateItem.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toggleUserStatus(selected.username(), false);
            }
        });
        
        MenuItem deleteItem = new MenuItem("Delete User");
        deleteItem.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteUser(selected.username());
            }
        });
        
        contextMenu.getItems().addAll(activateItem, deactivateItem, new SeparatorMenuItem(), deleteItem);
        usersTable.setContextMenu(contextMenu);
    }
    
    private void setupForm() {
        newRoleCombo.setItems(FXCollections.observableArrayList(User.Role.values()));
        newRoleCombo.setValue(User.Role.USER);
        
        // Bind create button
        createUserButton.disableProperty().bind(
            newUsernameField.textProperty().isEmpty()
                .or(newPasswordField.textProperty().isEmpty())
        );
        
        // Bind change password button
        changePasswordButton.disableProperty().bind(
            changePasswordUsernameField.textProperty().isEmpty()
                .or(newPasswordForUserField.textProperty().isEmpty())
        );
    }
    
    @FXML
    private void handleCreateUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        User.Role role = newRoleCombo.getValue();
        
        try {
            authService.createUser(username, password, role);
            showStatus("User created successfully: " + username, false);
            
            // Clear form
            newUsernameField.clear();
            newPasswordField.clear();
            newRoleCombo.setValue(User.Role.USER);
            
            refreshUsersList();
        } catch (AuthenticationException e) {
            showStatus("Failed to create user: " + e.getMessage(), true);
        }
    }
    
    @FXML
    private void handleChangePassword() {
        String username = changePasswordUsernameField.getText().trim();
        String newPassword = newPasswordForUserField.getText();
        
        try {
            authService.updatePassword(username, newPassword);
            showStatus("Password updated successfully for: " + username, false);
            
            // Clear form
            changePasswordUsernameField.clear();
            newPasswordForUserField.clear();
            
        } catch (AuthenticationException e) {
            showStatus("Failed to update password: " + e.getMessage(), true);
        }
    }
    
    @FXML
    private void handleRefresh() {
        refreshUsersList();
        showStatus("User list refreshed", false);
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) usersTable.getScene().getWindow();
        stage.close();
    }
    
    private void toggleUserStatus(String username, boolean activate) {
        // Prevent self-deactivation
        if (!activate && username.equals(currentSession.getUsername())) {
            showStatus("Cannot deactivate your own account", true);
            return;
        }
        
        try {
            if (activate) {
                authService.activateUser(username);
                showStatus("User activated: " + username, false);
            } else {
                authService.deactivateUser(username);
                showStatus("User deactivated: " + username, false);
            }
            refreshUsersList();
        } catch (AuthenticationException e) {
            showStatus("Failed to update user status: " + e.getMessage(), true);
        }
    }
    
    private void deleteUser(String username) {
        // Prevent self-deletion
        if (username.equals(currentSession.getUsername())) {
            showStatus("Cannot delete your own account", true);
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete user: " + username);
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                authService.deleteUser(username);
                showStatus("User deleted: " + username, false);
                refreshUsersList();
            } catch (AuthenticationException e) {
                showStatus("Failed to delete user: " + e.getMessage(), true);
            }
        }
    }
    
    private void refreshUsersList() {
        if (authService != null) {
            usersList.clear();
            usersList.addAll(authService.getAllUsers());
        }
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #2e7d32;");
        
        // Clear status after 5 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> statusLabel.setText("")));
        timeline.play();
    }
}