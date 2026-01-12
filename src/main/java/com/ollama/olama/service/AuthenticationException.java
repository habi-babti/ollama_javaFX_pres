package com.ollama.olama.service;

/**
 * Exception thrown for authentication-related errors.
 */
public class AuthenticationException extends Exception {
    
    public enum Type {
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        USER_INACTIVE,
        INVALID_USERNAME,
        INVALID_PASSWORD,
        CANNOT_DELETE_LAST_ADMIN,
        PERMISSION_DENIED
    }
    
    private final Type type;
    
    public AuthenticationException(Type type, String message) {
        super(message);
        this.type = type;
    }
    
    public AuthenticationException(Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(Type.INVALID_CREDENTIALS, "Invalid username or password");
    }
    
    public static AuthenticationException userNotFound(String username) {
        return new AuthenticationException(Type.USER_NOT_FOUND, "User not found: " + username);
    }
    
    public static AuthenticationException userAlreadyExists(String username) {
        return new AuthenticationException(Type.USER_ALREADY_EXISTS, "User already exists: " + username);
    }
    
    public static AuthenticationException userInactive(String username) {
        return new AuthenticationException(Type.USER_INACTIVE, "User account is inactive: " + username);
    }
    
    public static AuthenticationException invalidUsername(String reason) {
        return new AuthenticationException(Type.INVALID_USERNAME, "Invalid username: " + reason);
    }
    
    public static AuthenticationException invalidPassword(String reason) {
        return new AuthenticationException(Type.INVALID_PASSWORD, "Invalid password: " + reason);
    }
    
    public static AuthenticationException cannotDeleteLastAdmin() {
        return new AuthenticationException(Type.CANNOT_DELETE_LAST_ADMIN, "Cannot delete the last admin user");
    }
    
    public static AuthenticationException permissionDenied(String action) {
        return new AuthenticationException(Type.PERMISSION_DENIED, "Permission denied: " + action);
    }
}