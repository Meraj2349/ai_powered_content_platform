#!/bin/bash

# API Testing Script for AI Content Platform

BASE_URL="http://localhost:8080/api"
JWT_TOKEN=""

echo "🧪 AI Content Platform API Testing Script"
echo "=========================================="

# Function to make colored output
print_success() { echo -e "\033[0;32m✅ $1\033[0m"; }
print_error() { echo -e "\033[0;31m❌ $1\033[0m"; }
print_info() { echo -e "\033[0;34mℹ️  $1\033[0m"; }
print_warning() { echo -e "\033[1;33m⚠️  $1\033[0m"; }

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo ""
    print_info "Testing: $description"
    echo "🔗 $method $BASE_URL$endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ] && [ ! -z "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ] && [ ! -z "$JWT_TOKEN" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $JWT_TOKEN" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        print_success "Status: $http_code"
        echo "📄 Response: $body" | jq '.' 2>/dev/null || echo "📄 Response: $body"
    else
        print_error "Status: $http_code"
        echo "📄 Response: $body"
    fi
}

# Test 1: Health Check
test_endpoint "GET" "/health" "" "Application Health Check"

# Test 2: Database Health Check
test_endpoint "GET" "/health/database" "" "Database Health Check"

# Test 3: Ping Test
test_endpoint "GET" "/health/ping" "" "Ping Test"

# Test 4: User Registration
echo ""
print_info "🔐 Authentication Tests"
echo "========================"

USER_DATA='{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "name": "Test User"
}'

test_endpoint "POST" "/auth/register" "$USER_DATA" "User Registration"

# Test 5: User Login
LOGIN_DATA='{
  "username": "testuser",
  "password": "password123"
}'

echo ""
print_info "Testing user login..."
login_response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA" \
    "$BASE_URL/auth/login")

login_http_code=$(echo $login_response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
login_body=$(echo $login_response | sed -e 's/HTTPSTATUS:.*//g')

if [ "$login_http_code" -eq 200 ]; then
    print_success "Login successful - Status: $login_http_code"
    JWT_TOKEN=$(echo $login_body | jq -r '.token' 2>/dev/null)
    if [ "$JWT_TOKEN" != "null" ] && [ ! -z "$JWT_TOKEN" ]; then
        print_success "JWT Token extracted: ${JWT_TOKEN:0:20}..."
    else
        print_warning "Could not extract JWT token from response"
    fi
    echo "📄 Response: $login_body" | jq '.' 2>/dev/null || echo "📄 Response: $login_body"
else
    print_error "Login failed - Status: $login_http_code"
    echo "📄 Response: $login_body"
fi

# Test 6: Protected Content Generation (if we have token)
if [ ! -z "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
    echo ""
    print_info "🤖 AI Content Generation Tests"
    echo "==============================="
    
    CONTENT_DATA='{
      "prompt": "Write a short blog post about artificial intelligence",
      "contentType": "BLOG_POST",
      "maxTokens": 200
    }'
    
    echo ""
    print_info "Testing AI content generation..."
    content_response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$CONTENT_DATA" \
        "$BASE_URL/content/generate")
    
    content_http_code=$(echo $content_response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    content_body=$(echo $content_response | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$content_http_code" -eq 200 ] || [ "$content_http_code" -eq 201 ]; then
        print_success "Content generation - Status: $content_http_code"
        echo "📄 Response: $content_body" | jq '.' 2>/dev/null || echo "📄 Response: $content_body"
    else
        print_error "Content generation failed - Status: $content_http_code"
        echo "📄 Response: $content_body"
    fi
    
    # Test 7: Get User's Content
    echo ""
    print_info "Testing get user content...")
    user_content_response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X GET \
        -H "Authorization: Bearer $JWT_TOKEN" \
        "$BASE_URL/content/my-content")
    
    user_content_http_code=$(echo $user_content_response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    user_content_body=$(echo $user_content_response | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$user_content_http_code" -eq 200 ]; then
        print_success "Get user content - Status: $user_content_http_code"
        echo "📄 Response: $user_content_body" | jq '.' 2>/dev/null || echo "📄 Response: $user_content_body"
    else
        print_error "Get user content failed - Status: $user_content_http_code"
        echo "📄 Response: $user_content_body"
    fi
else
    print_warning "Skipping protected endpoint tests - no valid JWT token"
fi

echo ""
echo "🎉 API Testing Complete!"
echo ""
print_info "💡 Tips:"
echo "   • Check http://localhost:8081 for MongoDB Admin (admin/admin123)"
echo "   • Use Postman or curl for more detailed API testing"
echo "   • Check application logs for detailed error information"
