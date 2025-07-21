# MCP Server

A simple Model Context Protocol (MCP) server implementation in Java using Spring Boot.

## Overview

This project demonstrates how to implement an MCP server that provides tools and resources to AI models through a standardized interface. The server includes example tools like a calculator and weather information provider.

## Features

- **RESTful API**: Clean REST endpoints for tool listing and execution
- **Extensible Architecture**: Easy to add new tools and functionality
- **Error Handling**: Comprehensive error handling and logging
- **Testing**: Unit tests for core functionality
- **Documentation**: Well-documented code with Javadoc
- **Docker Support**: Containerized deployment ready

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Quick Start

### Running Locally

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd mcp-server
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. The server will start on `http://localhost:8080`

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t mcp-server .
   ```

2. Run the container:
   ```bash
   docker run -p 8080:8080 mcp-server
   ```

## API Endpoints

### List Tools
```
GET /api/v1/tools
```

Returns a list of all available tools with their descriptions and parameters.

### Execute Tool
```
POST /api/v1/tools/call
Content-Type: application/json

{
  "name": "calculator",
  "arguments": {
    "expression": "2 + 2"
  }
}
```

Executes a specific tool with the provided arguments.

### Health Check
```
GET /api/v1/health
```

Returns the server health status.

### Server Information
```
GET /api/v1/info
```

Returns server information and available endpoints.

## Available Tools

### Calculator
- **Name**: `calculator`
- **Description**: Performs basic mathematical calculations
- **Parameters**: 
  - `expression` (string, required): Mathematical expression to evaluate

### Weather
- **Name**: `weather`
- **Description**: Gets weather information for a location
- **Parameters**:
  - `location` (string, required): City or location name

## Adding New Tools

To add a new tool:

1. Define the tool in `McpService.initializeDefaultTools()`
2. Implement the tool logic in `ToolService.executeTool()`
3. Add appropriate tests

Example:
```java
// In McpService.initializeDefaultTools()
Tool myTool = new Tool();
myTool.setName("my_tool");
myTool.setDescription("Description of my tool");
// ... set parameters

// In ToolService.executeTool()
case "my_tool":
    return executeMyTool(arguments);
```

## Testing

Run the tests:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- MCP specification contributors 