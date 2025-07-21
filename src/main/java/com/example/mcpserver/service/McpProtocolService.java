package com.example.mcpserver.service;

import com.example.mcpserver.model.*;
import com.example.mcpserver.model.JsonRpcResponse.JsonRpcError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling MCP protocol operations.
 * 
 * This service processes JSON-RPC requests and notifications
 * according to the MCP specification.
 */
@Service
public class McpProtocolService {
    
    private static final Logger logger = LoggerFactory.getLogger(McpProtocolService.class);
    
    private final McpService mcpService;
    private final SessionService sessionService;
    
    @Autowired
    public McpProtocolService(McpService mcpService, SessionService sessionService) {
        this.mcpService = mcpService;
        this.sessionService = sessionService;
    }
    
    /**
     * Processes a JSON-RPC request and returns the appropriate response.
     * 
     * @param request The JSON-RPC request to process
     * @param session The current MCP session
     * @return The JSON-RPC response
     */
    public JsonRpcResponse processRequest(JsonRpcRequest request, McpSession session) {
        logger.debug("Processing request: {} with method: {}", request.getId(), request.getMethod());
        
        try {
            switch (request.getMethod()) {
                case "initialize":
                    return handleInitialize(request, session);
                case "tools/list":
                    return handleToolsList(request);
                case "tools/call":
                    return handleToolsCall(request);
                default:
                    return createErrorResponse(request.getId(), -32601, "Method not found");
            }
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage(), e);
            return createErrorResponse(request.getId(), -32603, "Internal error");
        }
    }
    
    /**
     * Processes a JSON-RPC notification.
     * 
     * @param notification The JSON-RPC notification to process
     * @param session The current MCP session
     */
    public void processNotification(JsonRpcNotification notification, McpSession session) {
        logger.debug("Processing notification with method: {}", notification.getMethod());
        
        try {
            switch (notification.getMethod()) {
                case "notifications/cancel":
                    handleCancelNotification(notification, session);
                    break;
                default:
                    logger.warn("Unknown notification method: {}", notification.getMethod());
            }
        } catch (Exception e) {
            logger.error("Error processing notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handles the initialize request.
     */
    private JsonRpcResponse handleInitialize(JsonRpcRequest request, McpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("capabilities", Map.of(
            "tools", Map.of(
                "listChanged", false
            ),
            "resources", Map.of(
                "subscribe", false,
                "listChanged", false
            )
        ));
        result.put("serverInfo", Map.of(
            "name", "MCP Server",
            "version", "1.0.0"
        ));
        
        session.setInitialized(true);
        logger.info("Initialized MCP session: {}", session.getSessionId());
        
        return new JsonRpcResponse(request.getId(), result);
    }
    
    /**
     * Handles the tools/list request.
     */
    private JsonRpcResponse handleToolsList(JsonRpcRequest request) {
        List<Tool> tools = mcpService.listTools();
        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);
        
        return new JsonRpcResponse(request.getId(), result);
    }
    
    /**
     * Handles the tools/call request.
     */
    private JsonRpcResponse handleToolsCall(JsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        if (params == null) {
            return createErrorResponse(request.getId(), -32602, "Invalid params");
        }
        
        String name = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        if (name == null) {
            return createErrorResponse(request.getId(), -32602, "Missing tool name");
        }
        
        try {
            ToolResult result = mcpService.callTool(new ToolCall(name, arguments));
            return new JsonRpcResponse(request.getId(), result);
        } catch (Exception e) {
            logger.error("Error calling tool {}: {}", name, e.getMessage(), e);
            return createErrorResponse(request.getId(), -32603, "Tool execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Handles the notifications/cancel notification.
     */
    private void handleCancelNotification(JsonRpcNotification notification, McpSession session) {
        Map<String, Object> params = notification.getParams();
        if (params != null) {
            Object requestId = params.get("requestId");
            logger.info("Received cancel notification for request: {} in session: {}", 
                       requestId, session.getSessionId());
            // TODO: Implement request cancellation logic
        }
    }
    
    /**
     * Creates an error response.
     */
    private JsonRpcResponse createErrorResponse(Object id, int code, String message) {
        JsonRpcError error = new JsonRpcError(code, message);
        return new JsonRpcResponse(id, error);
    }
} 