package com.example.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents a JSON-RPC request message.
 * 
 * This class contains the method name and parameters for a JSON-RPC request.
 */
public class JsonRpcRequest extends JsonRpcMessage {
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    public JsonRpcRequest() {}
    
    public JsonRpcRequest(Object id, String method, Map<String, Object> params) {
        super(id);
        this.method = method;
        this.params = params;
    }
    
    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
} 