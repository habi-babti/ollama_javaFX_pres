package com.ollama.olama.service;

import com.ollama.olama.model.LoginSession;
import com.ollama.olama.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling user authentication and management.
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username
     * @param password the plain text password
     * @return LoginSession if authentication successful, empty otherwise
     */
    Optional<LoginSession> authenticate(String username, String password);
    
    /**
     * Creates a new user account.
     * 
     * @param username the username
     * @param password the plain text password
     * @param role the user role
     * @return the created user
     * @throws AuthenticationException if user already exists or validation fails
     */
    User createUser(String username, String password, User.Role role) throws AuthenticationException;
    
    /**
     * Updates a user's password.
     * 
     * @param username the username
     * @param newPassword the new plain text password
     * @throws AuthenticationException if user not found
     */
    void updatePassword(String username, String newPassword) throws AuthenticationException;
    
    /**
     * Deactivates a user account.
     * 
     * @param username the username to deactivate
     * @throws AuthenticationException if user not found
     */
    void deactivateUser(String username) throws AuthenticationException;
    
    /**
     * Activates a user account.
     * 
     * @param username the username to activate
     * @throws AuthenticationException if user not found
     */
    void activateUser(String username) throws AuthenticationException;
    
    /**
     * Deletes a user account.
     * 
     * @param username the username to delete
     * @throws AuthenticationException if user not found or is the last admin
     */
    void deleteUser(String username) throws AuthenticationException;
    
    /**
     * Gets all users in the system.
     * 
     * @return list of all users
     */
    List<User> getAllUsers();
    
    /**
     * Gets a user by username.
     * 
     * @param username the username
     * @return the user if found, empty otherwise
     */
    Optional<User> getUser(String username);
    
    /**
     * Checks if a username is available.
     * 
     * @param username the username to check
     * @return true if available, false if taken
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Initializes the authentication system with default admin user if no users exist.
     */
    void initializeDefaultUsers();
}