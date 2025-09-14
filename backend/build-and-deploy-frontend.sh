#!/bin/bash

echo "Building and integrating frontend with backend..."

# Navigate to frontend directory
cd "/home/meraj/IdeaProjects/ai_powered_content_platform/Front-End/skillmate-frontend"

# Build the frontend
echo "Building frontend..."
npm run build

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Frontend build successful!"
    
    # Create static directory if it doesn't exist
    mkdir -p "/home/meraj/IdeaProjects/ai_powered_content_platform/backend/src/main/resources/static"
    
    # Remove old frontend files
    rm -rf "/home/meraj/IdeaProjects/ai_powered_content_platform/backend/src/main/resources/static"/*
    
    # Copy new build files
    cp -r dist/* "/home/meraj/IdeaProjects/ai_powered_content_platform/backend/src/main/resources/static/"
    
    echo "Frontend files copied to backend successfully!"
    echo "You can now start your backend server and access the frontend at http://localhost:8080"
else
    echo "Frontend build failed!"
    exit 1
fi