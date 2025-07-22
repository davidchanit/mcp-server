package com.example.mcpserver.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * CalculatorService - Offers arithmetic operations and expression evaluation for MCP
 *
 * @author mcp-server
 */
@Service
public class CalculatorService {

    @Tool(description = "Evaluate a mathematical expression (supports +, -, *, /, parentheses)")
    public double calculate(@ToolParam(description = "Expression to evaluate, e.g. 2 + 3 * 4") String expression) {
        try {
            // Evaluates a basic arithmetic expression
            return parseAndCompute(expression.replaceAll("\\s+", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input for calculation: " + e.getMessage());
        }
    }

    @Tool(description = "Return the sum of two numbers")
    public double add(@ToolParam(description = "First operand") double a,
                      @ToolParam(description = "Second operand") double b) {
        return a + b;
    }

    @Tool(description = "Return the difference between two numbers")
    public double subtract(@ToolParam(description = "Minuend") double a,
                           @ToolParam(description = "Subtrahend") double b) {
        return a - b;
    }

    @Tool(description = "Return the product of two numbers")
    public double multiply(@ToolParam(description = "First factor") double a,
                           @ToolParam(description = "Second factor") double b) {
        return a * b;
    }

    @Tool(description = "Return the quotient of two numbers")
    public double divide(@ToolParam(description = "Dividend") double a,
                         @ToolParam(description = "Divisor") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return a / b;
    }

    /**
     * Parses and computes a simple arithmetic expression.
     * Supports +, -, *, /, and parentheses.
     */
    private double parseAndCompute(String expr) {
        // Remove whitespace
        expr = expr.replaceAll("\\s+", "");

        // Handle parentheses recursively
        while (expr.contains("(")) {
            int open = expr.lastIndexOf("(");
            int close = expr.indexOf(")", open);
            if (close == -1) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            String inner = expr.substring(open + 1, close);
            double innerResult = parseAndCompute(inner);
            expr = expr.substring(0, open) + innerResult + expr.substring(close + 1);
        }

        // Handle multiplication and division
        String[] mulDivParts = expr.split("(?<=[*/])|(?=[*/])");
        if (mulDivParts.length > 1) {
            double result = Double.parseDouble(mulDivParts[0]);
            for (int i = 1; i < mulDivParts.length; i += 2) {
                if (i + 1 < mulDivParts.length) {
                    double operand = Double.parseDouble(mulDivParts[i + 1]);
                    if ("*".equals(mulDivParts[i])) {
                        result *= operand;
                    } else if ("/".equals(mulDivParts[i])) {
                        if (operand == 0) {
                            throw new IllegalArgumentException("Cannot divide by zero");
                        }
                        result /= operand;
                    }
                }
            }
            return result;
        }

        // Handle addition and subtraction
        String[] addSubParts = expr.split("(?<=[+-])|(?=[+-])");
        if (addSubParts.length > 1) {
            double result = Double.parseDouble(addSubParts[0]);
            for (int i = 1; i < addSubParts.length; i += 2) {
                if (i + 1 < addSubParts.length) {
                    double operand = Double.parseDouble(addSubParts[i + 1]);
                    if ("+".equals(addSubParts[i])) {
                        result += operand;
                    } else if ("-".equals(addSubParts[i])) {
                        result -= operand;
                    }
                }
            }
            return result;
        }

        // If only a single number remains
        return Double.parseDouble(expr);
    }
}