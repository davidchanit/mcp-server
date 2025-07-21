package com.example.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the MCP Server.
 * 
 * This server implements the Model Context Protocol (MCP) to provide
 * tools and resources to AI models through a standardized interface.
 * 
 * @author Your Name
 * @version 1.0.0
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
} 