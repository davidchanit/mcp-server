package com.example.mcpserver.service;

import com.example.mcpserver.model.Tool;
import com.example.mcpserver.model.ToolCall;
import com.example.mcpserver.model.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the McpService class.
 */
@SpringBootTest
class McpServiceTest {

    @Autowired
    private McpService mcpService;

    @Test
    void testListTools() {
        List<Tool> tools = mcpService.listTools();
        assertNotNull(tools);
        assertFalse(tools.isEmpty());
        
        // Check that calculator tool exists
        boolean hasCalculator = tools.stream()
                .anyMatch(tool -> "calculator".equals(tool.getName()));
        assertTrue(hasCalculator);
    }

    @Test
    void testCallCalculator() {
        ToolCall toolCall = new ToolCall();
        toolCall.setName("calculator");
        toolCall.setArguments(Map.of("expression", "2 + 2"));
        
        ToolResult result = mcpService.callTool(toolCall);
        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals(4.0, result.getContent());
    }

    @Test
    void testCallWeather() {
        ToolCall toolCall = new ToolCall();
        toolCall.setName("weather");
        toolCall.setArguments(Map.of("location", "New York"));
        
        ToolResult result = mcpService.callTool(toolCall);
        assertNotNull(result);
        assertFalse(result.isError());
        assertNotNull(result.getContent());
    }

    @Test
    void testCallNonExistentTool() {
        ToolCall toolCall = new ToolCall();
        toolCall.setName("non_existent_tool");
        toolCall.setArguments(Map.of());
        
        ToolResult result = mcpService.callTool(toolCall);
        assertNotNull(result);
        assertTrue(result.isError());
        assertTrue(result.getError().contains("Tool not found"));
    }
} 