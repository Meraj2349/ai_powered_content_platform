#!/bin/bash

# AI-Powered Content Platform Startup Script

echo "🚀 Starting AI-Powered Content Platform..."

# Set Java 17 as the runtime
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Set default environment variables if not already set
export OPENAI_API_KEY=${OPENAI_API_KEY:-"your-openai-api-key-here"}
export FIREBASE_CONFIG_PATH=${FIREBASE_CONFIG_PATH:-"src/main/resources/firebase-service-account.json"}

echo "📋 Environment Configuration:"
echo "   JAVA_HOME: $JAVA_HOME"
echo "   OPENAI_API_KEY: $(echo $OPENAI_API_KEY | cut -c1-10)..."
echo "   FIREBASE_CONFIG_PATH: $FIREBASE_CONFIG_PATH"

echo ""
echo "🍃 Checking MongoDB connection..."
if docker ps | grep -q "ai_content_mongodb"; then
    echo "✅ MongoDB container is running"
else
    echo "⚠️  MongoDB container not running. Starting MongoDB..."
    docker-compose up -d mongodb
    sleep 5
fi

echo ""
echo "🔧 Building application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful! Starting Spring Boot application..."
    echo "🌐 Application will be available at: http://localhost:8080"
    echo "📚 API Documentation:"
    echo "   Health Check: http://localhost:8080/api/health"
    echo "   Database Status: http://localhost:8080/api/health/database"
    echo "   Auth Endpoints: http://localhost:8080/api/auth/*"
    echo "   Content Endpoints: http://localhost:8080/api/content/*"
    echo ""
    echo "🔍 Useful commands while running:"
    echo "   Test Health: curl http://localhost:8080/api/health"
    echo "   Test Ping: curl http://localhost:8080/api/health/ping"
    echo ""
    mvn spring-boot:run
else
    echo "❌ Build failed! Please check the error messages above."
    exit 1
fi
