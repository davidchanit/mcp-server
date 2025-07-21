package com.example.mcpserver.service;

import com.example.mcpserver.model.Tool;
import com.example.mcpserver.model.ToolCall;
import com.example.mcpserver.model.ToolResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Main service for handling MCP operations.
 * 
 * This service manages tool registration, listing, and execution
 * according to the Model Context Protocol specification.
 */
@Service
public class McpService {
    
    private static final Logger logger = LoggerFactory.getLogger(McpService.class);
    
    private final Map<String, Tool> registeredTools = new HashMap<>();
    private final ToolService toolService;
    
    @Autowired
    public McpService(ToolService toolService) {
        this.toolService = toolService;
        initializeDefaultTools();
    }
    
    /**
     * Lists all available tools.
     * 
     * @return List of available tools
     */
    public List<Tool> listTools() {
        logger.info("Listing {} available tools", registeredTools.size());
        return new ArrayList<>(registeredTools.values());
    }
    
    /**
     * Calls a specific tool with the given arguments.
     * 
     * @param toolCall The tool call request
     * @return The result of the tool execution
     */
    public ToolResult callTool(ToolCall toolCall) {
        String toolName = toolCall.getName();
        logger.info("Calling tool: {} with arguments: {}", toolName, toolCall.getArguments());
        
        if (!registeredTools.containsKey(toolName)) {
            logger.error("Tool not found: {}", toolName);
            return ToolResult.error("Tool not found: " + toolName);
        }
        
        try {
            Object result = toolService.executeTool(toolName, toolCall.getArguments());
            logger.info("Tool {} executed successfully", toolName);
            return ToolResult.success(result);
        } catch (Exception e) {
            logger.error("Error executing tool {}: {}", toolName, e.getMessage(), e);
            return ToolResult.error("Error executing tool: " + e.getMessage());
        }
    }
    
    /**
     * Registers a new tool.
     * 
     * @param tool The tool to register
     */
    public void registerTool(Tool tool) {
        logger.info("Registering tool: {}", tool.getName());
        registeredTools.put(tool.getName(), tool);
    }
    
    /**
     * Initializes default tools that come with the server.
     */
    private void initializeDefaultTools() {
        // Calculator tool
        Tool calculator = new Tool();
        calculator.setName("calculator");
        calculator.setDescription("Performs basic mathematical calculations");
        
        List<Tool.Parameter> params = new ArrayList<>();
        params.add(new Tool.Parameter("expression", "string", "Mathematical expression to evaluate", true));
        calculator.setParameters(params);
        
        List<String> required = new ArrayList<>();
        required.add("expression");
        calculator.setRequired(required);
        
        registerTool(calculator);
        
        // Weather tool
        Tool weather = new Tool();
        weather.setName("weather");
        weather.setDescription("Gets weather information for a location");
        
        List<Tool.Parameter> weatherParams = new ArrayList<>();
        weatherParams.add(new Tool.Parameter("location", "string", "City or location name", true));
        weather.setParameters(weatherParams);
        
        List<String> weatherRequired = new ArrayList<>();
        weatherRequired.add("location");
        weather.setRequired(weatherRequired);
        
        registerTool(weather);
        
        logger.info("Initialized {} default tools", registeredTools.size());
    }
} 