package com.ollama.olama.model;

import java.time.LocalDateTime;

/**
 * Represents the current user session.
 */
public record LoginSession(
        User user,
        LocalDateTime loginTime
) {
    
    public LoginSession(User user) {
        this(user, LocalDateTime.now());
    }
    
    /**
     * Checks if the current user is an admin.
     */
    public boolean isAdmin() {
        return user.isAdmin();
    }
    
    /**
     * Gets the username of the current user.
     */
    public String getUsername() {
        return user.username();
    }
    
    /**
     * Gets the role of the current user.
     */
    public User.Role getRole() {
        return user.role();
    }
}