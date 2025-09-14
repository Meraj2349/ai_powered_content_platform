# MongoDB Setup Guide for SkillMate AI Platform

## 🗄️ Database Configuration

### Prerequisites

- MongoDB 4.4+ installed
- MongoDB Compass (optional, for GUI)

### Setup Steps

#### 1. Start MongoDB Service

```bash
# Ubuntu/Debian
sudo systemctl start mongod
sudo systemctl enable mongod

# macOS with Homebrew
brew services start mongodb-community

# Windows
net start MongoDB
```

#### 2. Access MongoDB Shell

```bash
mongosh
# or for older versions
mongo
```

#### 3. Run Initialization Script

```bash
# Execute the initialization script
mongosh ai_content_platform < scripts/mongodb-init.js

# Or manually run commands in MongoDB shell
```

#### 4. Manual Database Setup (Alternative)

##### Create Database and User

```javascript
// Switch to SkillMate AI database
use ai_content_platform;

// Create application user
db.createUser({
  user: "app_user",
  pwd: "app_password123",
  roles: [{ role: "readWrite", db: "ai_content_platform" }]
});
```

##### Create Collections with Schema Validation

```javascript
// Users collection
db.createCollection("users", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["username", "email", "password"],
      properties: {
        username: { bsonType: "string" },
        email: {
          bsonType: "string",
          pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        },
        password: { bsonType: "string", minLength: 8 },
      },
    },
  },
});

// Courses collection
db.createCollection("courses", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["title", "platform"],
      properties: {
        title: { bsonType: "string" },
        platform: {
          bsonType: "string",
          enum: ["UDEMY", "COURSERA", "EDX", "YOUTUBE", "GITHUB", "OTHER"],
        },
        qualityScore: { bsonType: "double", minimum: 0, maximum: 100 },
      },
    },
  },
});
```

##### Create Essential Indexes

```javascript
// User indexes
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });

// Course indexes
db.courses.createIndex({ title: 1 });
db.courses.createIndex({ platform: 1 });
db.courses.createIndex({ qualityScore: -1 });

// Text search indexes
db.courses.createIndex({
  title: "text",
  description: "text",
});
```

##### Insert Default Roles

```javascript
db.roles.insertMany([
  { name: "ROLE_USER" },
  { name: "ROLE_ADMIN" },
  { name: "ROLE_INSTRUCTOR" },
]);
```

## 📊 Database Schema

### Collections Overview

| Collection       | Purpose                            | Key Indexes                   |
| ---------------- | ---------------------------------- | ----------------------------- |
| `users`          | User accounts with Firebase sync   | username, email, firebaseUid  |
| `courses`        | Multi-platform course data         | title, platform, qualityScore |
| `reviews`        | Review analysis with sentiment     | courseId, rating, date        |
| `content`        | AI-generated educational content   | author, contentType, status   |
| `user_progress`  | Learning analytics and tracking    | userId, lastActivity          |
| `learning_paths` | Personalized learning journeys     | createdBy, difficulty         |
| `roles`          | Security and permission management | name                          |

### Performance Optimizations

#### Connection Pooling

```properties
# MongoDB connection pool settings
spring.data.mongodb.options.min-connections-per-host=10
spring.data.mongodb.options.max-connections-per-host=100
spring.data.mongodb.options.max-wait-time=120000
```

#### Query Optimization

- **Compound indexes** for common query patterns
- **Text indexes** for full-text search across courses and content
- **Sparse indexes** for optional fields like Firebase UID
- **TTL indexes** for temporary data (if needed)

#### Aggregation Pipeline Usage

```javascript
// Example: Course analytics aggregation
db.courses.aggregate([
  { $match: { platform: "UDEMY" } },
  {
    $group: {
      _id: "$platform",
      avgQuality: { $avg: "$qualityScore" },
      totalCourses: { $sum: 1 },
    },
  },
  { $sort: { avgQuality: -1 } },
]);
```

## 🔧 Configuration Properties

### Application Properties Setup

```properties
# MongoDB Configuration for SkillMate AI
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=ai_content_platform
spring.data.mongodb.username=app_user
spring.data.mongodb.password=app_password123
spring.data.mongodb.authentication-database=ai_content_platform
spring.data.mongodb.auto-index-creation=true

# Performance Settings
spring.data.mongodb.options.min-connections-per-host=10
spring.data.mongodb.options.max-connections-per-host=100
spring.data.mongodb.options.server-selection-timeout=30000
spring.data.mongodb.options.max-wait-time=120000
spring.data.mongodb.options.connect-timeout=10000
```

## 🧪 Testing Database Connection

### Test MongoDB Connection

```bash
# Test connection using mongosh
mongosh "mongodb://app_user:app_password123@localhost:27017/ai_content_platform"

# Test from Spring Boot application
curl http://localhost:8082/actuator/health
```

### Verify Collections and Indexes

```javascript
// Show all collections
show collections;

// Check indexes for users collection
db.users.getIndexes();

// Verify user count
db.users.countDocuments();

// Test text search
db.courses.find({ $text: { $search: "machine learning" } });
```

## 🔒 Security Considerations

### Production Setup

1. **Enable authentication** in MongoDB
2. **Use strong passwords** for database users
3. **Configure SSL/TLS** for encrypted connections
4. **Set up replica sets** for high availability
5. **Enable audit logging** for security monitoring

### Environment-Specific Configuration

```properties
# Development
spring.data.mongodb.host=localhost

# Production
spring.data.mongodb.uri=mongodb://user:pass@prod-mongo-cluster:27017/ai_content_platform?ssl=true&replicaSet=rs0
```

## 📈 Monitoring and Maintenance

### MongoDB Monitoring

- Use **MongoDB Compass** for visual monitoring
- Enable **profiling** for slow query analysis
- Set up **alerts** for performance metrics
- Monitor **connection pool** usage

### Backup Strategy

```bash
# Create backup
mongodump --db ai_content_platform --out /backup/skillmate-ai/

# Restore backup
mongorestore --db ai_content_platform /backup/skillmate-ai/ai_content_platform/
```

## 🚀 Production Deployment

### Docker Configuration

```yaml
version: "3.8"
services:
  mongodb:
    image: mongo:7.0
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: adminpassword
      MONGO_INITDB_DATABASE: ai_content_platform
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
      - ./scripts/mongodb-init.js:/docker-entrypoint-initdb.d/init.js

volumes:
  mongodb_data:
```

### Cloud Deployment (MongoDB Atlas)

```properties
# MongoDB Atlas connection
spring.data.mongodb.uri=mongodb+srv://app_user:password@skillmate-cluster.mongodb.net/ai_content_platform?retryWrites=true&w=majority
```

Your MongoDB configuration is now complete and optimized for the SkillMate AI Platform! 🎉
