# Spring AI MCP Server

A Model Context Protocol (MCP) server implementation using Spring AI framework with custom HTTP transport. This server provides tools for mathematical calculations and games, accessible via JSON-RPC over HTTP.

## Features

- ✅ **Custom HTTP MCP Server**: Implements MCP protocol over HTTP with JSON-RPC
- ✅ **Spring AI Framework Integration**: Built on Spring AI MCP framework
- ✅ **Tool Auto-Discovery**: Automatic tool registration via `@Tool` annotations
- ✅ **Multiple Transport Support**: HTTP (custom) + SSE (framework-provided)
- ✅ **Real-time Communication**: Support for both HTTP and SSE transport methods
- ✅ **Cursor IDE Integration**: Compatible with Cursor's MCP client

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Cursor IDE (for testing MCP integration)

### 1. Build the Project

```bash
mvn clean package -DskipTests
```

### 2. Run the Server

```bash
java -jar target/mcp-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

The server will start on port 8090.

### 3. Test the API

Test the MCP endpoint:

```bash
# Test ping
curl -X POST http://localhost:8090/api/v1/mpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "id": 1, "method": "ping", "params": {}}'

# Test calculate tool
curl -X POST http://localhost:8090/api/v1/mpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "id": 2, "method": "tools/call", "params": {"name": "calculate", "arguments": {"expression": "22+2"}}}'
```

## Configuration

### Server Configuration (application.yml)

```yaml
server:
  port: 8090

spring:
  application:
    name: mcp-server

ai:
  mcp:
    server:
      name: mcp-server # MCP server name
      version: 0.0.1   # Server version

logging:
  level:
    org.springframework.ai: DEBUG
    com.example.mcpserver: DEBUG
```

### Cursor IDE Configuration

Add this to your Cursor MCP configuration file (`~/.cursor/mcp.json`):

```json
{
  "mcpServers": {
    "spring-ai-mcp-server": {
      "command": "http",
      "args": {
        "url": "http://localhost:8090/api/v1/mpc"
      }
    }
  }
}
```

## Available Tools

### Calculator Tools (CalculatorService)

- **calculate**: Execute basic mathematical expressions
- **add**: Add two numbers
- **subtract**: Subtract two numbers
- **multiply**: Multiply two numbers
- **divide**: Divide two numbers

### Game Tools (GameService)

- **rockPaperScissors**: Play Rock, Paper, Scissors - randomly returns one of the three options
- **playRockPaperScissors**: Play Rock, Paper, Scissors against a computer - you choose your move
- **getRandomChoice**: Get a random choice from Rock, Paper, Scissors

## Adding New Tools

1. Create a new service class with `@Service` annotation
2. Add `@Tool` annotation to methods
3. Add `@ToolParam` annotation to parameters

Example:

```java
@Service
public class MyToolsService {

    @Tool(description = "Tool description")
    public String myTool(@ToolParam(description = "Parameter description") String parameter) {
        // Tool implementation
        return "Result: " + parameter;
    }
}
```

## Project Structure

```
src/main/java/com/example/mcpserver/
├── McpServerApplication.java          # Main application class
├── controller/
│   └── McpController.java            # Custom MCP HTTP controller
├── service/
│   ├── CalculatorService.java        # Calculator tools service
│   └── GameService.java              # Game tools service
└── resources/
    └── application.yml               # Application configuration
```

## Transport Methods

This project supports multiple transport methods:

### 1. HTTP Transport (Custom Implementation)
- **Endpoint**: `POST http://localhost:8090/api/v1/mpc`
- **Protocol**: JSON-RPC over HTTP
- **Usage**: Direct HTTP requests, Cursor IDE integration
- **Response**: Immediate JSON responses

### 2. SSE Transport (Framework-Provided)
- **Endpoint**: Automatically provided by Spring AI framework
- **Protocol**: Server-Sent Events for streaming
- **Usage**: Real-time, persistent connections
- **Response**: Streaming events

## Dependencies

### Core Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

### Repository Configuration

```xml
<repositories>
    <repository>
        <id>spring-snapshots</id>
        <name>Spring Snapshots</name>
        <url>https://repo.spring.io/snapshot</url>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**: Change the port in `application.yml`
2. **Tools Not Appearing**: Ensure service classes have `@Service` annotation
3. **API Not Responding**: Check if server is running on correct port
4. **Cursor Connection Issues**: Verify MCP configuration in `~/.cursor/mcp.json`

### Logs

The application uses SLF4J logging. Check console output for detailed log information.

## Development

### Building for Development

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Docker Support

Build Docker image:

```bash
# Build the JAR first
mvn clean package -DskipTests

# Build Docker image
docker build -t mcp-server .
```

Run with Docker:

```bash
# Run with Docker
docker run -p 8090:8090 mcp-server

# Or run with docker-compose
docker-compose up -d
```

Test Docker container:

```bash
curl -X POST http://localhost:8090/api/v1/mpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "id": 1, "method": "ping", "params": {}}'
```

### CI/CD Testing

For CI/CD pipelines, use the provided test script:

```bash
# Make script executable
chmod +x ci-test.sh

# Run the test
./ci-test.sh
```

Or manually:

```bash
docker run -d --name mcp-test -p 8090:8090 mcp-server:latest
sleep 10
curl -f -X POST http://localhost:8090/api/v1/mpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "id": 1, "method": "ping", "params": {}}' || exit 1
docker stop mcp-test
docker rm mcp-test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
