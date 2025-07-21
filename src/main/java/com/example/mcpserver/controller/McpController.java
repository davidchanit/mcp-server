package com.example.mcpserver.controller;

import com.example.mcpserver.model.Tool;
import com.example.mcpserver.model.ToolCall;
import com.example.mcpserver.model.ToolResult;
import com.example.mcpserver.service.McpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST controller for MCP operations.
 * 
 * This controller provides endpoints for listing tools and executing tool calls
 * according to the Model Context Protocol specification.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class McpController {
    
    private static final Logger logger = LoggerFactory.getLogger(McpController.class);
    
    private final McpService mcpService;
    
    @Autowired
    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }
    
    /**
     * Lists all available tools.
     * 
     * @return List of available tools
     */
    @GetMapping("/tools")
    public ResponseEntity<List<Tool>> listTools() {
        logger.info("Received request to list tools");
        List<Tool> tools = mcpService.listTools();
        return ResponseEntity.ok(tools);
    }
    
    /**
     * Calls a tool with the provided arguments.
     * 
     * @param toolCall The tool call request
     * @return The result of the tool execution
     */
    @PostMapping("/tools/call")
    public ResponseEntity<ToolResult> callTool(@Valid @RequestBody ToolCall toolCall) {
        logger.info("Received tool call request: {}", toolCall.getName());
        ToolResult result = mcpService.callTool(toolCall);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Health check endpoint.
     * 
     * @return Server status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = Map.of(
            "status", "UP",
            "service", "MCP Server",
            "version", "1.0.0"
        );
        return ResponseEntity.ok(status);
    }
    
    /**
     * Server information endpoint.
     * 
     * @return Server information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = Map.of(
            "name", "MCP Server",
            "version", "1.0.0",
            "description", "A simple Model Context Protocol (MCP) server implementation in Java",
            "protocol", "MCP v1.0",
            "endpoints", List.of(
                "GET /api/v1/tools - List available tools",
                "POST /api/v1/tools/call - Execute a tool",
                "GET /api/v1/health - Health check",
                "GET /api/v1/info - Server information"
            )
        );
        return ResponseEntity.ok(info);
    }
} 