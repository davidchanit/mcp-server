package com.example.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents a JSON-RPC notification message.
 * 
 * This class contains the method name and parameters for a JSON-RPC notification.
 * Notifications do not have an ID and do not expect a response.
 */
public class JsonRpcNotification extends JsonRpcMessage {
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    public JsonRpcNotification() {}
    
    public JsonRpcNotification(String method, Map<String, Object> params) {
        super(null); // Notifications don't have IDs
        this.method = method;
        this.params = params;
    }
    
    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
} 