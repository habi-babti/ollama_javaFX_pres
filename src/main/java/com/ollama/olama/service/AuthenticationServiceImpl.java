package com.ollama.olama.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ollama.olama.model.LoginSession;
import com.ollama.olama.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of AuthenticationService that stores users in a JSON file.
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private static final String USERS_FILE = "ollama-chat-users.json";
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SALT_LENGTH = 16;
    
    private final Path usersFilePath;
    private final ObjectMapper objectMapper;
    private final Map<String, User> users;
    private final SecureRandom random;
    
    public AuthenticationServiceImpl() {
        this.usersFilePath = Paths.get(System.getProperty("user.home"), ".ollama-chat", USERS_FILE);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.users = new ConcurrentHashMap<>();
        this.random = new SecureRandom();
        
        // Create directory if it doesn't exist
        try {
            Files.createDirectories(usersFilePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create users directory", e);
        }
        
        loadUsers();
        initializeDefaultUsers();
    }
    
    @Override
    public Optional<LoginSession> authenticate(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        User user = users.get(username.toLowerCase());
        if (user == null || !user.isActive()) {
            return Optional.empty();
        }
        
        if (verifyPassword(password, user.passwordHash())) {
            // Update last login time
            User updatedUser = user.withLastLogin(LocalDateTime.now());
            users.put(username.toLowerCase(), updatedUser);
            saveUsers();
            
            return Optional.of(new LoginSession(updatedUser));
        }
        
        return Optional.empty();
    }
    
    @Override
    public User createUser(String username, String password, User.Role role) throws AuthenticationException {
        validateUsername(username);
        validatePassword(password);
        
        String normalizedUsername = username.toLowerCase();
        if (users.containsKey(normalizedUsername)) {
            throw AuthenticationException.userAlreadyExists(username);
        }
        
        String passwordHash = hashPassword(password);
        User user = new User(username, passwordHash, role, LocalDateTime.now(), null, true);
        
        users.put(normalizedUsername, user);
        saveUsers();
        
        return user;
    }
    
    @Override
    public void updatePassword(String username, String newPassword) throws AuthenticationException {
        validatePassword(newPassword);
        
        String normalizedUsername = username.toLowerCase();
        User user = users.get(normalizedUsername);
        if (user == null) {
            throw AuthenticationException.userNotFound(username);
        }
        
        String newPasswordHash = hashPassword(newPassword);
        User updatedUser = user.withPasswordHash(newPasswordHash);
        users.put(normalizedUsername, updatedUser);
        saveUsers();
    }
    
    @Override
    public void deactivateUser(String username) throws AuthenticationException {
        String normalizedUsername = username.toLowerCase();
        User user = users.get(normalizedUsername);
        if (user == null) {
            throw AuthenticationException.userNotFound(username);
        }
        
        // Check if this is the last admin
        if (user.isAdmin() && countActiveAdmins() <= 1) {
            throw AuthenticationException.cannotDeleteLastAdmin();
        }
        
        User updatedUser = user.withActiveStatus(false);
        users.put(normalizedUsername, updatedUser);
        saveUsers();
    }
    
    @Override
    public void activateUser(String username) throws AuthenticationException {
        String normalizedUsername = username.toLowerCase();
        User user = users.get(normalizedUsername);
        if (user == null) {
            throw AuthenticationException.userNotFound(username);
        }
        
        User updatedUser = user.withActiveStatus(true);
        users.put(normalizedUsername, updatedUser);
        saveUsers();
    }
    
    @Override
    public void deleteUser(String username) throws AuthenticationException {
        String normalizedUsername = username.toLowerCase();
        User user = users.get(normalizedUsername);
        if (user == null) {
            throw AuthenticationException.userNotFound(username);
        }
        
        // Check if this is the last admin
        if (user.isAdmin() && countActiveAdmins() <= 1) {
            throw AuthenticationException.cannotDeleteLastAdmin();
        }
        
        users.remove(normalizedUsername);
        saveUsers();
    }
    
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public Optional<User> getUser(String username) {
        return Optional.ofNullable(users.get(username.toLowerCase()));
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        return username != null && !username.trim().isEmpty() && 
               !users.containsKey(username.toLowerCase());
    }
    
    @Override
    public void initializeDefaultUsers() {
        if (users.isEmpty()) {
            try {
                // Create default admin user
                createUser("admin", "admin123", User.Role.ADMIN);
                System.out.println("Created default admin user: admin/admin123");
            } catch (AuthenticationException e) {
                throw new RuntimeException("Failed to create default admin user", e);
            }
        }
    }
    
    private void validateUsername(String username) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw AuthenticationException.invalidUsername("Username cannot be empty");
        }
        
        if (username.length() < 3) {
            throw AuthenticationException.invalidUsername("Username must be at least 3 characters");
        }
        
        if (username.length() > 50) {
            throw AuthenticationException.invalidUsername("Username cannot exceed 50 characters");
        }
        
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            throw AuthenticationException.invalidUsername("Username can only contain letters, numbers, underscore, and dash");
        }
    }
    
    private void validatePassword(String password) throws AuthenticationException {
        if (password == null || password.isEmpty()) {
            throw AuthenticationException.invalidPassword("Password cannot be empty");
        }
        
        if (password.length() < 6) {
            throw AuthenticationException.invalidPassword("Password must be at least 6 characters");
        }
        
        if (password.length() > 100) {
            throw AuthenticationException.invalidPassword("Password cannot exceed 100 characters");
        }
    }
    
    private String hashPassword(String password) {
        String salt = generateSalt();
        return salt + ":" + hashWithSalt(password, salt);
    }
    
    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        
        String[] parts = storedHash.split(":", 2);
        String salt = parts[0];
        String hash = parts[1];
        
        return hash.equals(hashWithSalt(password, salt));
    }
    
    private String generateSalt() {
        StringBuilder salt = new StringBuilder(SALT_LENGTH);
        for (int i = 0; i < SALT_LENGTH; i++) {
            salt.append(SALT_CHARS.charAt(random.nextInt(SALT_CHARS.length())));
        }
        return salt.toString();
    }
    
    private String hashWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes());
            byte[] hashedBytes = md.digest();
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    private long countActiveAdmins() {
        return users.values().stream()
                .filter(user -> user.isAdmin() && user.isActive())
                .count();
    }
    
    private void loadUsers() {
        if (!Files.exists(usersFilePath)) {
            return;
        }
        
        try {
            List<User> userList = objectMapper.readValue(
                usersFilePath.toFile(), 
                new TypeReference<List<User>>() {}
            );
            
            users.clear();
            for (User user : userList) {
                users.put(user.username().toLowerCase(), user);
            }
        } catch (IOException e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
    }
    
    private void saveUsers() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(usersFilePath.toFile(), new ArrayList<>(users.values()));
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }
}