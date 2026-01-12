package com.ollama.olama.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user in the system with authentication and role information.
 */
public record User(
        @JsonProperty("username") String username,
        @JsonProperty("passwordHash") String passwordHash,
        @JsonProperty("role") Role role,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("lastLogin") LocalDateTime lastLogin,
        @JsonProperty("isActive") boolean isActive
) {
    
    public enum Role {
        USER, ADMIN
    }
    
    @JsonCreator
    public User(
            @JsonProperty("username") String username,
            @JsonProperty("passwordHash") String passwordHash,
            @JsonProperty("role") Role role,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("lastLogin") LocalDateTime lastLogin,
            @JsonProperty("isActive") boolean isActive
    ) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.lastLogin = lastLogin;
        this.isActive = isActive;
    }
    
    /**
     * Creates a new user with USER role.
     */
    public static User createUser(String username, String passwordHash) {
        return new User(username, passwordHash, Role.USER, LocalDateTime.now(), null, true);
    }
    
    /**
     * Creates a new admin user.
     */
    public static User createAdmin(String username, String passwordHash) {
        return new User(username, passwordHash, Role.ADMIN, LocalDateTime.now(), null, true);
    }
    
    /**
     * Updates the last login time.
     */
    public User withLastLogin(LocalDateTime lastLogin) {
        return new User(username, passwordHash, role, createdAt, lastLogin, isActive);
    }
    
    /**
     * Updates the active status.
     */
    public User withActiveStatus(boolean isActive) {
        return new User(username, passwordHash, role, createdAt, lastLogin, isActive);
    }
    
    /**
     * Updates the password hash.
     */
    public User withPasswordHash(String newPasswordHash) {
        return new User(username, newPasswordHash, role, createdAt, lastLogin, isActive);
    }
    
    /**
     * Checks if user has admin privileges.
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    /**
     * Returns user for display (without sensitive data).
     */
    public String getDisplayName() {
        return username + " (" + role.name().toLowerCase() + ")";
    }
}