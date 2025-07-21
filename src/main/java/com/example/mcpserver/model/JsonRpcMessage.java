package com.example.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base class for JSON-RPC messages according to the MCP specification.
 * 
 * This class provides the common structure for all JSON-RPC messages
 * including requests, responses, and notifications.
 */
// Remove polymorphic deserialization - we'll handle it manually
public abstract class JsonRpcMessage {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    @JsonProperty("id")
    private Object id;
    
    public JsonRpcMessage() {}
    
    public JsonRpcMessage(Object id) {
        this.id = id;
    }
    
    // Getters and Setters
    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
    
    public Object getId() { return id; }
    public void setId(Object id) { this.id = id; }
    
    /**
     * Checks if this message has an ID (making it a request/response).
     */
    public boolean hasId() {
        return id != null;
    }
    
    /**
     * Checks if this message is a notification (no ID).
     */
    public boolean isNotification() {
        return id == null;
    }
} 