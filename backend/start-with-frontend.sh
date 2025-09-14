#!/bin/bash

echo "Starting SkillMate AI Platform..."

# Set environment variables (update these with your actual values)
export SERVER_PORT=8080
export MONGODB_DATABASE_NAME=skillmate_db
export MONGODB_AUTO_INDEX_CREATION=true
export MONGODB_PORT=27017
export MONGODB_HOST=localhost
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-email-password
export MAIL_AUTH=true
export MAIL_STARTTLS=true
export JWT_KEY=your-jwt-secret-key-here-make-it-long-and-secure
export REFRESH_TOKEN_KEY=your-refresh-token-key-here-make-it-long-and-secure
export CORS_ALLOWED_ORIGIN_1=http://localhost:3000
export AI_ANALYZER_BASE_URL=http://localhost:5000

echo "Environment variables set."

# Navigate to backend directory
cd "/home/meraj/IdeaProjects/ai_powered_content_platform/backend"

# Build and run the application
echo "Building application with integrated frontend..."
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev

echo "Application started! Access your frontend at: http://localhost:8080"