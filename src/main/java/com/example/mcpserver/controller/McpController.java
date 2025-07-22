package com.example.mcpserver.controller;

import com.example.mcpserver.service.CalculatorService;
import com.example.mcpserver.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MCP API Controller - Handles endpoints for Model Context Protocol (MCP) requests.
 */
@RestController
@CrossOrigin(origins = "*")
public class McpController {

    private final AtomicLong eventIdCounter = new AtomicLong(0);

    @Autowired
    private CalculatorService calculatorService;

    @Autowired
    private GameService gameService;

    /**
     * Main entry for MCP protocol POST requests.
     */
    @PostMapping(value = "/api/v1/mpc", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleMcpRequest(@RequestBody Map<String, Object> request) {
        String method = (String) request.get("method");
        Object id = request.get("id");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");

        try {
            switch (method) {
                case "initialize":
                    return ResponseEntity.ok(buildInitializeResponse(id));
                case "tools/list":
                    return ResponseEntity.ok(buildToolsListResponse(id));
                case "tools/call":
                    return ResponseEntity.ok(buildToolsCallResponse(id, params));
                case "notifications/list":
                    return ResponseEntity.ok(buildNotificationsListResponse(id));
                case "ping":
                    return ResponseEntity.ok(buildPingResponse(id));
                default:
                    return ResponseEntity.ok(buildErrorResponse(id, -32601, "Method not found: " + method));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(buildErrorResponse(id, -32603, "Internal error: " + e.getMessage()));
        }
    }

    private Map<String, Object> buildInitializeResponse(Object id) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("capabilities", Map.of(
            "tools", Map.of("listChanged", false),
            "notifications", Map.of("listChanged", false)
        ));
        result.put("serverInfo", Map.of(
            "name", "Spring AI MCP Server",
            "version", "1.0.0"
        ));

        response.put("result", result);
        return response;
    }

    private Map<String, Object> buildToolsListResponse(Object id) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        Map<String, Object> result = new HashMap<>();
        result.put("tools", listAvailableTools());

        response.put("result", result);
        return response;
    }

    private Map<String, Object> buildToolsCallResponse(Object id, Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        try {
            String name = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

            Object result = runTool(name, arguments);

            Map<String, Object> resultMap = new HashMap<>();
            // MCP content format: type and text
            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("type", "text");
            contentItem.put("text", String.valueOf(result));
            resultMap.put("content", new Object[]{contentItem});
            response.put("result", resultMap);

        } catch (Exception e) {
            response.put("error", Map.of(
                "code", -32603,
                "message", "Tool execution error: " + e.getMessage()
            ));
        }

        return response;
    }

    private Map<String, Object> buildNotificationsListResponse(Object id) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        Map<String, Object> result = new HashMap<>();
        result.put("notifications", new Object[0]); // No notifications available

        response.put("result", result);
        return response;
    }

    private Map<String, Object> buildPingResponse(Object id) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        Map<String, Object> result = new HashMap<>();
        result.put("pong", System.currentTimeMillis());

        response.put("result", result);
        return response;
    }

    private Map<String, Object> buildErrorResponse(Object id, int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of(
            "code", code,
            "message", message
        ));
        return response;
    }

    /**
     * Returns the list of available tools with descriptions and input schemas.
     */
    private Object[] listAvailableTools() {
        return new Object[]{
            Map.of(
                "name", "calculate",
                "description", "Evaluate a mathematical expression (supports +, -, *, /, parentheses).",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "expression", Map.of(
                            "type", "string",
                            "description", "Math expression, e.g. 2 + 3 * 4"
                        )
                    ),
                    "required", new String[]{"expression"}
                )
            ),
            Map.of(
                "name", "add",
                "description", "Return the sum of two numbers.",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "a", Map.of(
                            "type", "number",
                            "description", "First number"
                        ),
                        "b", Map.of(
                            "type", "number",
                            "description", "Second number"
                        )
                    ),
                    "required", new String[]{"a", "b"}
                )
            ),
            Map.of(
                "name", "subtract",
                "description", "Return the difference between two numbers.",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "a", Map.of(
                            "type", "number",
                            "description", "First number"
                        ),
                        "b", Map.of(
                            "type", "number",
                            "description", "Second number"
                        )
                    ),
                    "required", new String[]{"a", "b"}
                )
            ),
            Map.of(
                "name", "multiply",
                "description", "Return the product of two numbers.",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "a", Map.of(
                            "type", "number",
                            "description", "First number"
                        ),
                        "b", Map.of(
                            "type", "number",
                            "description", "Second number"
                        )
                    ),
                    "required", new String[]{"a", "b"}
                )
            ),
            Map.of(
                "name", "divide",
                "description", "Return the quotient of two numbers.",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "a", Map.of(
                            "type", "number",
                            "description", "First number"
                        ),
                        "b", Map.of(
                            "type", "number",
                            "description", "Second number"
                        )
                    ),
                    "required", new String[]{"a", "b"}
                )
            ),
            Map.of(
                "name", "rockPaperScissors",
                "description", "Play Rock, Paper, Scissors - randomly returns one of the three options",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(),
                    "required", new String[]{}
                )
            ),
            Map.of(
                "name", "playRockPaperScissors",
                "description", "Play Rock, Paper, Scissors against a computer - you choose your move",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "playerChoice", Map.of(
                            "type", "string",
                            "description", "Your choice: rock, paper, or scissors"
                        )
                    ),
                    "required", new String[]{"playerChoice"}
                )
            ),
            Map.of(
                "name", "getRandomChoice",
                "description", "Get a random choice from Rock, Paper, Scissors",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(),
                    "required", new String[]{}
                )
            )
        };
    }

    /**
     * Executes the requested tool by name with provided arguments.
     */
    private Object runTool(String name, Map<String, Object> arguments) {
        switch (name) {
            case "calculate":
                String expression = (String) arguments.get("expression");
                return calculatorService.calculate(expression);
            case "add":
                double a = ((Number) arguments.get("a")).doubleValue();
                double b = ((Number) arguments.get("b")).doubleValue();
                return calculatorService.add(a, b);
            case "subtract":
                double a2 = ((Number) arguments.get("a")).doubleValue();
                double b2 = ((Number) arguments.get("b")).doubleValue();
                return calculatorService.subtract(a2, b2);
            case "multiply":
                double a3 = ((Number) arguments.get("a")).doubleValue();
                double b3 = ((Number) arguments.get("b")).doubleValue();
                return calculatorService.multiply(a3, b3);
            case "divide":
                double a4 = ((Number) arguments.get("a")).doubleValue();
                double b4 = ((Number) arguments.get("b")).doubleValue();
                return calculatorService.divide(a4, b4);
            case "rockPaperScissors":
                return gameService.rockPaperScissors();
            case "playRockPaperScissors":
                String playerChoice = (String) arguments.get("playerChoice");
                return gameService.playRockPaperScissors(playerChoice);
            case "getRandomChoice":
                return gameService.getRandomChoice();
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }
    }
}