package com.example.mcpserver.service;

import com.example.mcpserver.model.McpSession;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing MCP sessions.
 * 
 * This service handles session creation, retrieval, and cleanup
 * according to the MCP Streamable HTTP transport specification.
 */
@Service
public class SessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();
    
    /**
     * Creates a new MCP session.
     * 
     * @return The newly created session
     */
    public McpSession createSession() {
        McpSession session = new McpSession();
        sessions.put(session.getSessionId(), session);
        logger.info("Created new MCP session: {}", session.getSessionId());
        return session;
    }
    
    /**
     * Retrieves a session by its ID.
     * 
     * @param sessionId The session ID to look up
     * @return Optional containing the session if found
     */
    public Optional<McpSession> getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        McpSession session = sessions.get(sessionId);
        if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            logger.info("Removed expired session: {}", sessionId);
            return Optional.empty();
        }
        
        return Optional.ofNullable(session);
    }
    
    /**
     * Validates a session ID format.
     * 
     * @param sessionId The session ID to validate
     * @return true if the session ID is valid
     */
    public boolean isValidSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        
        // Check if it contains only visible ASCII characters (0x21 to 0x7E)
        return sessionId.chars().allMatch(ch -> ch >= 0x21 && ch <= 0x7E);
    }
    
    /**
     * Terminates a session.
     * 
     * @param sessionId The session ID to terminate
     * @return true if the session was found and terminated
     */
    public boolean terminateSession(String sessionId) {
        McpSession removed = sessions.remove(sessionId);
        if (removed != null) {
            logger.info("Terminated MCP session: {}", sessionId);
            return true;
        }
        return false;
    }
    
    /**
     * Cleans up expired sessions.
     */
    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                logger.debug("Removing expired session: {}", entry.getKey());
            }
            return expired;
        });
    }
    
    /**
     * Gets the current number of active sessions.
     * 
     * @return The number of active sessions
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
} 