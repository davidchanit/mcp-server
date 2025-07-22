#!/bin/bash
set -e

echo "🚀 Deploying MCP Server to Heroku..."

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo "❌ Heroku CLI is not installed. Please install it first:"
    echo "   https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Build the application
echo "📦 Building application..."
mvn clean package -DskipTests

# Check if Heroku app exists, create if not
if ! heroku apps:info &> /dev/null; then
    echo "🔧 Creating new Heroku app..."
    heroku create
fi

# Get the app name
APP_NAME=$(heroku apps:info --json | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
echo "📱 Heroku app: $APP_NAME"

# Set environment variables
echo "⚙️ Setting environment variables..."
heroku config:set SPRING_PROFILES_ACTIVE=prod

# Deploy to Heroku
echo "🚀 Deploying to Heroku..."
git add .
git commit -m "Deploy MCP server to Heroku" || true
git push heroku main

# Open the app
echo "🌐 Opening the app..."
heroku open

echo "✅ Deployment complete!"
echo "📱 App URL: https://$APP_NAME.herokuapp.com"
echo "🧪 Test Client: https://$APP_NAME.herokuapp.com/mcp-client.html" 