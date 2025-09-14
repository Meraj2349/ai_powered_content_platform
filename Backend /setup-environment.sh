#!/bin/bash

# AI Content Platform Environment Setup

echo "🔧 Setting up environment variables for AI Content Platform..."

# OpenAI Configuration
echo "🤖 OpenAI API Setup:"
echo "Please enter your OpenAI API key (starts with sk-...):"
read -p "OpenAI API Key: " OPENAI_API_KEY

if [ ! -z "$OPENAI_API_KEY" ]; then
    export OPENAI_API_KEY="$OPENAI_API_KEY"
    echo "export OPENAI_API_KEY=\"$OPENAI_API_KEY\"" >> ~/.bashrc
    echo "✅ OpenAI API key configured"
else
    echo "⚠️  No OpenAI API key provided. Using default placeholder."
    export OPENAI_API_KEY="your-openai-api-key"
fi

# Firebase Configuration
echo ""
echo "🔥 Firebase Setup:"
read -p "Do you have a Firebase service account JSON file? (y/n): " HAS_FIREBASE

if [ "$HAS_FIREBASE" = "y" ] || [ "$HAS_FIREBASE" = "Y" ]; then
    read -p "Enter path to your Firebase service account JSON file: " FIREBASE_PATH
    if [ -f "$FIREBASE_PATH" ]; then
        cp "$FIREBASE_PATH" src/main/resources/firebase-service-account.json
        export FIREBASE_CONFIG_PATH="src/main/resources/firebase-service-account.json"
        echo "✅ Firebase configuration file copied"
    else
        echo "⚠️  Firebase file not found. Using default path."
    fi
else
    echo "⚠️  No Firebase configuration provided. Firebase features will be disabled."
fi

# Java Configuration
echo ""
echo "☕ Java Setup:"
if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    echo "export JAVA_HOME=\"/usr/lib/jvm/java-17-openjdk-amd64\"" >> ~/.bashrc
    echo "✅ JAVA_HOME set to Java 17"
else
    echo "⚠️  Java 17 not found. Please install Java 17."
fi

# Display current configuration
echo ""
echo "📋 Current Configuration:"
echo "   OPENAI_API_KEY: $(echo $OPENAI_API_KEY | cut -c1-10)..."
echo "   FIREBASE_CONFIG_PATH: ${FIREBASE_CONFIG_PATH:-'not configured'}"
echo "   JAVA_HOME: $JAVA_HOME"

echo ""
echo "🎉 Environment setup completed!"
echo "💡 Tip: Restart your terminal or run 'source ~/.bashrc' to load new environment variables."
