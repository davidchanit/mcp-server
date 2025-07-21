# MCP Server with Streamable HTTP Transport

This project implements a Model Context Protocol (MCP) server using the Streamable HTTP transport specification. The server provides tools and resources to AI models through a standardized interface with support for streaming responses and server-to-client notifications.

## Features

- **Streamable HTTP Transport**: Full implementation of the MCP Streamable HTTP transport specification
- **Session Management**: Secure session handling with unique session IDs
- **SSE Support**: Server-Sent Events for streaming responses and notifications
- **Security**: Origin header validation and localhost-only binding
- **JSON-RPC**: Complete JSON-RPC 2.0 message handling
- **Tool Integration**: Built-in calculator and weather tools with extensible architecture
- **Test Client**: Web-based test client for demonstrating the transport

## Architecture

### Core Components

1. **McpEndpointController**: Main endpoint handling POST/GET/DELETE requests
2. **McpProtocolService**: JSON-RPC message processing and MCP protocol logic
3. **SessionService**: Session lifecycle management
4. **McpService**: Tool registration and execution
5. **ToolService**: Individual tool implementations

### Transport Implementation

The server implements the MCP Streamable HTTP transport with the following features:

- **Single Endpoint**: `/mcp` handles all MCP communication
- **POST Requests**: Send JSON-RPC messages (requests, notifications, responses)
- **GET Requests**: Open SSE streams for server-to-client communication
- **DELETE Requests**: Terminate sessions
- **Session Headers**: `Mcp-Session-Id` for session management
- **SSE Headers**: `Last-Event-ID` for stream resumability

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Server

1. **Clone and build the project**:
   ```bash
   git clone <repository-url>
   cd mcp-server
   mvn clean install
   ```

2. **Generate SSL keystore (for HTTPS)**:
   ```bash
   ./generate-keystore.sh
   ```
   *Note: This creates a self-signed certificate for development. For production, use a proper SSL certificate.*

3. **Start the server**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the test client**:
   - HTTP: `http://127.0.0.1:8080/mcp-client.html`
   - HTTPS: `https://127.0.0.1:8080/mcp-client.html` (accept the self-signed certificate warning)

### Testing with the Web Client

1. **Connect**: Click "Connect" to establish a session
2. **Initialize**: Select "initialize" from the dropdown and send the request
3. **List Tools**: Select "tools/list" to see available tools
4. **Call Tools**: Select "tools/call" and modify the arguments to test tool execution

## API Reference

### MCP Endpoint: `/mcp`

#### POST Request
Sends JSON-RPC messages to the server.

**Headers**:
- `Content-Type: application/json`
- `Accept: application/json, text/event-stream`
- `Mcp-Session-Id: <session-id>` (optional)

**Body**: JSON-RPC message or array of messages

**Responses**:
- `200 OK`: Single JSON response
- `202 Accepted`: Notifications/responses accepted
- `400 Bad Request`: Invalid request format
- `403 Forbidden`: Invalid Origin header

#### GET Request
Opens an SSE stream for server-to-client communication.

**Headers**:
- `Accept: text/event-stream`
- `Mcp-Session-Id: <session-id>` (optional)
- `Last-Event-ID: <event-id>` (optional, for resumability)

**Response**: `200 OK` with `Content-Type: text/event-stream`

#### DELETE Request
Terminates a session.

**Headers**:
- `Mcp-Session-Id: <session-id>`

**Response**: `200 OK` if session terminated successfully

### JSON-RPC Messages

#### Request
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "calculator",
    "arguments": {
      "expression": "2 + 2"
    }
  }
}
```

#### Response
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": 4
}
```

#### Notification
```json
{
  "jsonrpc": "2.0",
  "method": "notifications/cancel",
  "params": {
    "requestId": 1
  }
}
```

### MCP Protocol Methods

#### initialize
Initializes the MCP session and returns server capabilities.

**Parameters**:
- `protocolVersion`: Protocol version (e.g., "2024-11-05")
- `capabilities`: Client capabilities
- `clientInfo`: Client information

**Response**:
- `protocolVersion`: Server protocol version
- `capabilities`: Server capabilities
- `serverInfo`: Server information

#### tools/list
Lists all available tools.

**Parameters**: None

**Response**:
- `tools`: Array of available tools

#### tools/call
Executes a tool with the given arguments.

**Parameters**:
- `name`: Tool name
- `arguments`: Tool arguments

**Response**:
- `content`: Array of content items with tool results

## SSL Configuration

The server supports both HTTP and HTTPS connections. For development, a self-signed certificate is used.

### Development SSL Setup

1. **Generate keystore**:
   ```bash
   ./generate-keystore.sh
   ```

2. **SSL configuration** (already configured in `application.yml`):
   ```yaml
   server:
     ssl:
       enabled: true
       key-store: classpath:keystore.p12
       key-store-password: password
       key-store-type: PKCS12
       key-alias: mcp-server
   ```

3. **Access via HTTPS**:
   - Server: `https://localhost:8080/mcp`
   - Test client: `https://localhost:8080/mcp-client.html`

### Production SSL Setup

For production environments, replace the self-signed certificate with a proper SSL certificate:

1. **Obtain SSL certificate** from a trusted CA
2. **Convert to PKCS12 format**:
   ```bash
   openssl pkcs12 -export -in certificate.crt -inkey private.key -out keystore.p12
   ```
3. **Update configuration** with your certificate details
4. **Set secure passwords** in environment variables

### Cursor Integration

To connect Cursor to your MCP server, update your `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "mcp-server": {
      "url": "https://localhost:8080/mcp"
    }
  }
}
```

## Built-in Tools

### Calculator Tool
Performs basic mathematical calculations.

**Name**: `calculator`

**Parameters**:
- `expression` (string, required): Mathematical expression

**Example**:
```json
{
  "name": "calculator",
  "arguments": {
    "expression": "(2 + 3) * 4"
  }
}
```

### Weather Tool
Returns mock weather information for a location.

**Name**: `weather`

**Parameters**:
- `location` (string, required): City or location name

**Example**:
```json
{
  "name": "weather",
  "arguments": {
    "location": "New York"
  }
}
```

## Security Considerations

### DNS Rebinding Protection
The server validates the `Origin` header to prevent DNS rebinding attacks:

- Allows requests without Origin header (localhost)
- Validates Origin against Host header for localhost
- Should be configured with proper origin validation in production

### Localhost Binding
The server binds only to `127.0.0.1` by default for security:

```yaml
server:
  address: 127.0.0.1
  port: 8080
```

### Session Security
- Session IDs are cryptographically secure UUIDs
- Sessions expire after 24 hours
- Session IDs contain only visible ASCII characters

## Configuration

### Application Properties
```yaml
server:
  port: 8080
  address: 127.0.0.1  # Bind to localhost only

spring:
  application:
    name: mcp-server
  
  jackson:
    default-property-inclusion: non_null

logging:
  level:
    com.example.mcpserver: INFO
```

### Customization
To add new tools:

1. Implement tool logic in `ToolService.executeTool()`
2. Register the tool in `McpService.initializeDefaultTools()`
3. Define tool parameters and schema

## Development

### Project Structure
```
src/main/java/com/example/mcpserver/
├── McpServerApplication.java          # Main application class
├── controller/
│   ├── McpController.java            # Legacy REST API
│   └── McpEndpointController.java    # MCP Streamable HTTP endpoint
├── model/
│   ├── JsonRpcMessage.java           # Base JSON-RPC message
│   ├── JsonRpcRequest.java           # JSON-RPC request
│   ├── JsonRpcResponse.java          # JSON-RPC response
│   ├── JsonRpcNotification.java      # JSON-RPC notification
│   ├── McpSession.java               # Session management
│   ├── Tool.java                     # Tool definition
│   ├── ToolCall.java                 # Tool call request
│   └── ToolResult.java               # Tool execution result
└── service/
    ├── McpProtocolService.java       # MCP protocol handling
    ├── McpService.java               # Main MCP service
    ├── SessionService.java           # Session management
    └── ToolService.java              # Tool implementations
```

### Building
```bash
mvn clean compile
mvn test
mvn package
```

### Running Tests
```bash
mvn test
```

## Troubleshooting

### Common Issues

1. **Connection Refused**: Ensure the server is running on `127.0.0.1:8080`
2. **CORS Errors**: The server allows all origins in development mode
3. **Session Issues**: Check that session IDs are properly formatted
4. **SSE Stream Errors**: Verify the client accepts `text/event-stream`

### Logs
Enable debug logging for detailed information:
```yaml
logging:
  level:
    com.example.mcpserver: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## References

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [MCP Streamable HTTP Transport](https://modelcontextprotocol.io/spec/transport/streamable-http)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)
- [Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
