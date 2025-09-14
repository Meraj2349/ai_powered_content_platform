# 🚀 Quick Start Guide - AI Content Platform

## Prerequisites Checklist

- ✅ **Java 17** installed
- ✅ **Maven** installed
- ✅ **Docker & Docker Compose** installed
- ✅ **MongoDB** running in Docker
- ⚠️ **OpenAI API Key** (required for AI features)
- ⚠️ **Firebase Config** (optional)

## 🎯 Step-by-Step Launch

### 1. **Set Up Environment**

```bash
# Run the environment setup script
./setup-environment.sh
```

This will prompt you for:

- OpenAI API key
- Firebase configuration file (optional)

### 2. **Start MongoDB** (if not already running)

```bash
# Check if MongoDB is running
docker ps | grep mongo

# If not running, start it
docker-compose up -d
```

### 3. **Start the Application**

```bash
# Start the Spring Boot application
./start.sh
```

### 4. **Test the Application**

```bash
# Run API tests
./test-api.sh
```

## 🌐 Application URLs

| Service             | URL                                       | Description                    |
| ------------------- | ----------------------------------------- | ------------------------------ |
| **Main API**        | http://localhost:8080/api                 | Spring Boot Application        |
| **Health Check**    | http://localhost:8080/api/health          | Application Health             |
| **Database Health** | http://localhost:8080/api/health/database | MongoDB Status                 |
| **MongoDB Admin**   | http://localhost:8081                     | Mongo Express (admin/admin123) |

## 🔑 API Endpoints

### **Authentication**

```bash
# Register User
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}

# Login
POST /api/auth/login
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

### **Content Generation** (Requires JWT Token)

```bash
# Generate AI Content
POST /api/content/generate
Authorization: Bearer <jwt-token>
{
  "prompt": "Write a blog post about AI",
  "contentType": "BLOG_POST",
  "maxTokens": 500
}

# Get User's Content
GET /api/content/my-content
Authorization: Bearer <jwt-token>
```

## 🔧 Configuration

### **OpenAI API Key**

1. Get your key from [OpenAI Platform](https://platform.openai.com/)
2. Set environment variable:
   ```bash
   export OPENAI_API_KEY="sk-your-actual-key-here"
   ```

### **Firebase (Optional)**

1. Download service account JSON from Firebase Console
2. Place it in `src/main/resources/firebase-service-account.json`

## 🐛 Troubleshooting

### **Common Issues:**

| Problem                       | Solution                              |
| ----------------------------- | ------------------------------------- |
| **Port 8080 already in use**  | `sudo lsof -ti:8080 \| xargs kill -9` |
| **MongoDB connection failed** | `docker-compose restart`              |
| **OpenAI API errors**         | Check your API key and billing        |
| **Compilation errors**        | `mvn clean compile`                   |

### **Check Application Status:**

```bash
# Application health
curl http://localhost:8080/api/health

# Database status
curl http://localhost:8080/api/health/database

# MongoDB containers
docker ps | grep mongo

# Application logs
docker-compose logs -f
```

## 🎉 Success Indicators

When everything is working correctly, you should see:

1. ✅ **MongoDB service running**

   ```bash
   sudo systemctl status mongod
   # Should show: Active (running)
   ```

2. ✅ **Application starts without errors**

   ```bash
   # Should see: "Started Application in X.XX seconds"
   ```

3. ✅ **Health checks pass**

   ```bash
   curl http://localhost:8080/api/health
   # Should return: {"status":"UP",...}
   ```

4. ✅ **API tests pass**
   ```bash
   ./test-api.sh
   # Should show green checkmarks ✅
   ```

## 🚀 Next Steps

After successful launch:

1. **Test user registration and login**
2. **Generate sample AI content**
3. **Explore MongoDB Admin UI**
4. **Customize configurations as needed**
5. **Add your own content types and features**

---

**Happy Coding! 🎯**
