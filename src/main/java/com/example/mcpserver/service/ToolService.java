package com.example.mcpserver.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;
import java.util.HashMap;

/**
 * Service for executing individual tools.
 * 
 * This service contains the actual implementation of each tool
 * and handles the execution logic.
 */
@Service
public class ToolService {
    
    private static final Logger logger = LoggerFactory.getLogger(ToolService.class);
    
    /**
     * Executes a tool with the given arguments.
     * 
     * @param toolName The name of the tool to execute
     * @param arguments The arguments for the tool
     * @return The result of the tool execution
     * @throws Exception if the tool execution fails
     */
    public Object executeTool(String toolName, Map<String, Object> arguments) throws Exception {
        logger.debug("Executing tool: {} with arguments: {}", toolName, arguments);
        
        switch (toolName) {
            case "calculator":
                return executeCalculator(arguments);
            case "weather":
                return executeWeather(arguments);
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }
    
    /**
     * Executes the calculator tool.
     * 
     * @param arguments Tool arguments containing the expression
     * @return The calculated result
     * @throws Exception if calculation fails
     */
    private Object executeCalculator(Map<String, Object> arguments) throws Exception {
        String expression = (String) arguments.get("expression");
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression is required");
        }
        
        // Basic security check - only allow mathematical expressions
        if (!expression.matches("^[0-9+\\-*/().\\s]+$")) {
            throw new IllegalArgumentException("Invalid characters in expression");
        }
        
        try {
            // Simple expression evaluator for basic arithmetic
            String sanitizedExpression = expression.replaceAll("\\s+", "");
            double result = evaluateExpression(sanitizedExpression);
            logger.info("Calculator result: {} = {}", expression, result);
            return result;
        } catch (NumberFormatException e) {
            throw new Exception("Invalid mathematical expression: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid mathematical expression: " + e.getMessage());
        }
    }
    
    /**
     * Simple expression evaluator for basic arithmetic operations.
     * 
     * @param expression The mathematical expression to evaluate
     * @return The result of the calculation
     */
    private double evaluateExpression(String expression) {
        // Remove all spaces
        expression = expression.replaceAll("\\s+", "");
        
        // Handle parentheses first
        while (expression.contains("(")) {
            int start = expression.lastIndexOf("(");
            int end = expression.indexOf(")", start);
            if (end == -1) {
                throw new IllegalArgumentException("Unmatched parentheses");
            }
            String subExpression = expression.substring(start + 1, end);
            double subResult = evaluateExpression(subExpression);
            expression = expression.substring(0, start) + subResult + expression.substring(end + 1);
        }
        
        // Handle multiplication and division
        String[] parts = expression.split("(?<=[*/])|(?=[*/])");
        if (parts.length > 1) {
            double result = Double.parseDouble(parts[0]);
            for (int i = 1; i < parts.length; i += 2) {
                if (i + 1 < parts.length) {
                    double operand = Double.parseDouble(parts[i + 1]);
                    if (parts[i].equals("*")) {
                        result *= operand;
                    } else if (parts[i].equals("/")) {
                        if (operand == 0) {
                            throw new IllegalArgumentException("Division by zero");
                        }
                        result /= operand;
                    }
                }
            }
            return result;
        }
        
        // Handle addition and subtraction
        parts = expression.split("(?<=[+-])|(?=[+-])");
        if (parts.length > 1) {
            double result = Double.parseDouble(parts[0]);
            for (int i = 1; i < parts.length; i += 2) {
                if (i + 1 < parts.length) {
                    double operand = Double.parseDouble(parts[i + 1]);
                    if (parts[i].equals("+")) {
                        result += operand;
                    } else if (parts[i].equals("-")) {
                        result -= operand;
                    }
                }
            }
            return result;
        }
        
        // Single number
        return Double.parseDouble(expression);
    }
    
    /**
     * Executes the weather tool.
     * 
     * @param arguments Tool arguments containing the location
     * @return Mock weather information
     * @throws Exception if weather lookup fails
     */
    private Object executeWeather(Map<String, Object> arguments) throws Exception {
        String location = (String) arguments.get("location");
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        
        // Mock weather data - in a real implementation, this would call a weather API
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("location", location);
        weatherData.put("temperature", "22°C");
        weatherData.put("condition", "Sunny");
        weatherData.put("humidity", "65%");
        weatherData.put("wind", "10 km/h");
        weatherData.put("note", "This is mock data. In production, integrate with a real weather API.");
        
        logger.info("Weather lookup for {}: {}", location, weatherData);
        return weatherData;
    }
} 