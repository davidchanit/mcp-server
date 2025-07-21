package com.example.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents a call to a tool with its parameters.
 * 
 * This class encapsulates the information needed to execute a tool,
 * including the tool name and its input parameters.
 */
public class ToolCall {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("arguments")
    private Map<String, Object> arguments;
    
    @JsonProperty("callId")
    private String callId;
    
    public ToolCall() {}
    
    public ToolCall(String name, Map<String, Object> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Map<String, Object> getArguments() { return arguments; }
    public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
    
    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }
} 