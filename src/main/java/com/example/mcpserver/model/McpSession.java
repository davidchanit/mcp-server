package com.example.mcpserver.model;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Represents an MCP session between a client and server.
 * 
 * This class manages session state, including the session ID,
 * creation time, and any session-specific data.
 */
public class McpSession {
    
    private final String sessionId;
    private final Instant createdAt;
    private final Map<String, Object> sessionData;
    private boolean initialized;
    
    public McpSession() {
        this.sessionId = generateSessionId();
        this.createdAt = Instant.now();
        this.sessionData = new ConcurrentHashMap<>();
        this.initialized = false;
    }
    
    public McpSession(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = Instant.now();
        this.sessionData = new ConcurrentHashMap<>();
        this.initialized = false;
    }
    
    /**
     * Generates a cryptographically secure session ID.
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Gets the session ID.
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the session creation time.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Checks if the session has been initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Marks the session as initialized.
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    /**
     * Stores a value in the session data.
     */
    public void putData(String key, Object value) {
        sessionData.put(key, value);
    }
    
    /**
     * Retrieves a value from the session data.
     */
    public Object getData(String key) {
        return sessionData.get(key);
    }
    
    /**
     * Removes a value from the session data.
     */
    public Object removeData(String key) {
        return sessionData.remove(key);
    }
    
    /**
     * Checks if the session has expired (older than 24 hours).
     */
    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(24 * 60 * 60)); // 24 hours
    }
    
    @Override
    public String toString() {
        return "McpSession{" +
                "sessionId='" + sessionId + '\'' +
                ", createdAt=" + createdAt +
                ", initialized=" + initialized +
                '}';
    }
} 