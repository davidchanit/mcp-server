package com.example.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents the result of a tool execution.
 * 
 * This class contains the output data from a tool call, including
 * the result content and any metadata about the execution.
 */
public class ToolResult {
    
    @JsonProperty("content")
    private Object content;
    
    @JsonProperty("isError")
    private boolean isError;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    public ToolResult() {}
    
    public ToolResult(Object content) {
        this.content = content;
        this.isError = false;
    }
    
    public static ToolResult success(Object content) {
        return new ToolResult(content);
    }
    
    public static ToolResult error(String error) {
        ToolResult result = new ToolResult();
        result.isError = true;
        result.error = error;
        return result;
    }
    
    // Getters and Setters
    public Object getContent() { return content; }
    public void setContent(Object content) { this.content = content; }
    
    public boolean isError() { return isError; }
    public void setIsError(boolean isError) { this.isError = isError; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
} 