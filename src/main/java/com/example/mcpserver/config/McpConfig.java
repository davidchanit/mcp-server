package com.example.mcpserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for the MCP server.
 * 
 * This class handles CORS configuration and other web-related settings.
 */
@Configuration
public class McpConfig implements WebMvcConfigurer {
    
    /**
     * Configures CORS settings for the application.
     * 
     * @param registry The CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        
        registry.addMapping("/mcp/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Mcp-Session-Id")
                .maxAge(3600);
    }
} 