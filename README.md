# MCP Server - Java Implementation

[![CI](https://github.com/david.chan/mcp-server/workflows/Continuous%20Integration/badge.svg)](https://github.com/david.chan/mcp-server/actions/workflows/ci.yml)
[![CD](https://github.com/david.chan/mcp-server/workflows/Continuous%20Deployment/badge.svg)](https://github.com/david.chan/mcp-server/actions/workflows/cd.yml)
[![CodeQL](https://github.com/david.chan/mcp-server/workflows/CodeQL/badge.svg)](https://github.com/david.chan/mcp-server/actions/workflows/codeql.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://docker.com/)

A production-ready Model Context Protocol (MCP) server implementation in Java using Spring Boot.

## 🚀 Quick Start

### Local Development
```bash
# Clone the repository
git clone https://github.com/david.chan/mcp-server.git
cd mcp-server

# Run locally
mvn spring-boot:run
```

### Docker
```bash
# Build and run with Docker
docker build -t mcp-server .
docker run -p 8080:8080 mcp-server
```

### GitHub Container Registry
```bash
# Pull from GHCR
docker pull ghcr.io/david.chan/mcp-server:latest
docker run -p 8080:8080 ghcr.io/david.chan/mcp-server:latest
```

### Prerequisites
- Java 17
- Maven 3.9+
- Docker (for Docker testing)
- curl (for API testing)

### Step 1: Local Build Testing

```bash
# Navigate to your project directory
cd mcp-server

# Clean and build
mvn clean package

# Run tests
mvn test

# Check if JAR is created
ls -la target/mcp-server-1.0.0.jar

# Verify JAR is executable
java -jar target/mcp-server-1.0.0.jar --version
```

### Step 2: Docker Testing

```bash
# Build Docker image
docker build -t mcp-server-test .

# Test Docker container
docker run -d --name mcp-test -p 8080:8080 mcp-server-test

# Wait for container to start
sleep 10

# Test health endpoint
curl -f http://localhost:8080/api/v1/health

# Test tools endpoint
curl -f http://localhost:8080/api/v1/tools

# Clean up
docker stop mcp-test
docker rm mcp-test
```

### Step 3: API Testing

Start the server first (either locally or with Docker), then test the API:

```bash
# Health check
curl -f http://localhost:8080/api/v1/health

# List available tools
curl -f http://localhost:8080/api/v1/tools

# Test calculator tool
curl -X POST http://localhost:8080/api/v1/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "calculator",
    "arguments": {
      "expression": "2 + 2 * 3"
    }
  }'

# Test weather tool
curl -X POST http://localhost:8080/api/v1/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "weather",
    "arguments": {
      "location": "New York"
    }
  }'

# Test invalid tool
curl -X POST http://localhost:8080/api/v1/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "invalid_tool",
    "arguments": {}
  }'
```

### Step 4: Integration Testing

```bash
# Run integration tests
mvn verify

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Step 5: Performance Testing

```bash
# Test with multiple concurrent requests
for i in {1..10}; do
  curl -s http://localhost:8080/api/v1/health &
done
wait

# Test calculator with complex expressions
curl -X POST http://localhost:8080/api/v1/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "calculator",
    "arguments": {
      "expression": "(10 + 5) * 2 / 3 - 1"
    }
  }'
```

### Step 6: CI/CD Pipeline Testing

```bash
# Make a small change to trigger CI
echo "" >> README.md
echo "## 🧪 Testing CI/CD Pipeline" >> README.md
echo "This section was added to test the GitHub Actions workflow." >> README.md

# Commit and push
git add README.md
git commit -m "test: trigger CI/CD pipeline for testing"
git push origin main

# Create a release tag to test deployment
git tag v1.0.1
git push origin v1.0.1
```

### Step 7: Monitoring and Debugging

```bash
# Check application logs
docker logs mcp-test

# Monitor system resources
docker stats mcp-test

# Test actuator endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
curl http://localhost:8080/actuator/metrics

# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## 📊 CI/CD Pipeline

This project uses GitHub Actions for continuous integration and deployment:

- **Continuous Integration**: Runs tests, builds JAR, security scans
- **Continuous Deployment**: Deploys to GitHub Container Registry on releases
- **Code Quality**: CodeQL analysis for security vulnerabilities
- **Dependency Updates**: Automated dependency updates via Dependabot

### Monitoring CI/CD

1. **Go to your GitHub repository**
2. **Click on "Actions" tab**
3. **Watch workflow runs in real-time**

### Expected CI/CD Results

✅ **Test & Build Job:**
- Checkout code
- Set up JDK 17
- Cache Maven packages
- Run tests
- Build JAR
- Upload artifacts

✅ **Security Scan Job:**
- OWASP dependency check
- Upload security report

✅ **Docker Build Job:**
- Build Docker image
- Test Docker container
- Upload Docker image

## 🔧 Development

### Prerequisites
- Java 17
- Maven 3.9+
- Docker (optional)

### Running Tests
```bash
mvn test
```

### Building
```bash
mvn clean package
```

## 📈 Monitoring

- Health check: `GET /api/v1/health`
- Metrics: `GET /actuator/metrics`
- Info: `GET /actuator/info`

## 🚨 Troubleshooting

### Common Issues

**Tests Fail:**
```bash
# Run tests with debug info
mvn test -X

# Check for specific test failures
mvn test -Dtest=McpServiceTest
```

**Docker Build Fails:**
```bash
# Test Docker build locally
docker build -t test-image .
docker run -d --name test-container -p 8080:8080 test-image
sleep 10
curl http://localhost:8080/api/v1/health
docker stop test-container
docker rm test-container
```

**Security Scan Fails:**
```bash
# Update dependencies
mvn versions:use-latest-versions
mvn clean package
```

**Port Already in Use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
docker run -p 8081:8080 mcp-server
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

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Testing Before Contributing

```bash
# Run all tests
mvn clean test

# Check code coverage
mvn jacoco:report

# Build and test Docker
docker build -t mcp-server .
docker run -d --name test-container -p 8080:8080 mcp-server
sleep 10
curl http://localhost:8080/api/v1/health
docker stop test-container
docker rm test-container
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔧 CI/CD Fix - Mon Jul 21 14:35:21 HKT 2025
Fixed artifact upload issues in GitHub Actions workflow.
