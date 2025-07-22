#!/bin/bash
set -e

echo "Starting MCP Server Docker container test..."

# Run the container with correct port mapping
docker run -d --name mcp-test -p 8090:8090 mcp-server:latest

echo "Waiting for container to start..."
sleep 10

# Test the MCP ping endpoint (POST)
echo "Testing MCP ping endpoint (POST)..."
curl -f -X POST http://localhost:8090/api/v1/mpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "id": 1, "method": "ping", "params": {}}' || exit 1

# Test the MCP SSE endpoint (GET)
echo "Testing MCP SSE endpoint (GET)..."
curl -f -X GET http://localhost:8090/api/v1/mpc \
  -H "Accept: text/event-stream" || exit 1

echo "MCP Server test successful!"

# Clean up
docker stop mcp-test
docker rm mcp-test

echo "Test completed successfully!" 