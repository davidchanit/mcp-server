package com.example.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a JSON-RPC response message.
 * 
 * This class contains either a result or an error for a JSON-RPC response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse extends JsonRpcMessage {
    
    @JsonProperty("result")
    private Object result;
    
    @JsonProperty("error")
    private JsonRpcError error;
    
    public JsonRpcResponse() {}
    
    public JsonRpcResponse(Object id, Object result) {
        super(id);
        this.result = result;
    }
    
    public JsonRpcResponse(Object id, JsonRpcError error) {
        super(id);
        this.error = error;
    }
    
    // Getters and Setters
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
    
    public JsonRpcError getError() { return error; }
    public void setError(JsonRpcError error) { this.error = error; }
    
    /**
     * Checks if this response contains an error.
     */
    public boolean hasError() {
        return error != null;
    }
    
    /**
     * Represents a JSON-RPC error.
     */
    public static class JsonRpcError {
        @JsonProperty("code")
        private int code;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("data")
        private Object data;
        
        public JsonRpcError() {}
        
        public JsonRpcError(int code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public JsonRpcError(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
        
        // Getters and Setters
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
} 