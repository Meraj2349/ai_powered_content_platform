// MongoDB Initialization Script for SkillMate AI Platform
// Run this script in MongoDB shell: mongosh < scripts/mongodb-init.js

// Switch to SkillMate AI database
db = db.getSiblingDB('ai_content_platform');

// Create application user with read/write permissions
db.createUser({
  user: "app_user",
  pwd: "app_password123",
  roles: [
    {
      role: "readWrite",
      db: "ai_content_platform"
    }
  ]
});

// Create collections with validation schemas
db.createCollection("users", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["username", "email", "password"],
      properties: {
        username: {
          bsonType: "string",
          description: "Username must be a string and is required"
        },
        email: {
          bsonType: "string",
          pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
          description: "Email must be valid and is required"
        },
        password: {
          bsonType: "string",
          minLength: 8,
          description: "Password must be at least 8 characters"
        },
        firebaseUid: {
          bsonType: "string",
          description: "Firebase user ID for authentication sync"
        },
        firebaseVerified: {
          bsonType: "bool",
          description: "Firebase email verification status"
        }
      }
    }
  }
});

db.createCollection("courses", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["title", "platform"],
      properties: {
        title: {
          bsonType: "string",
          description: "Course title is required"
        },
        platform: {
          bsonType: "string",
          enum: ["UDEMY", "COURSERA", "EDX", "YOUTUBE", "GITHUB", "OTHER"],
          description: "Platform must be one of the supported platforms"
        },
        qualityScore: {
          bsonType: "double",
          minimum: 0,
          maximum: 100,
          description: "Quality score between 0 and 100"
        },
        technologies: {
          bsonType: "array",
          description: "List of technologies covered in the course"
        }
      }
    }
  }
});

db.createCollection("reviews", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["courseId", "userId", "overallRating"],
      properties: {
        courseId: {
          bsonType: "string",
          description: "Course ID is required"
        },
        userId: {
          bsonType: "string",
          description: "User ID is required"
        },
        overallRating: {
          bsonType: "int",
          minimum: 1,
          maximum: 5,
          description: "Rating must be between 1 and 5"
        },
        reviewText: {
          bsonType: "string",
          description: "Review text content"
        },
        sentimentAnalysis: {
          bsonType: "object",
          description: "AI-generated sentiment analysis results"
        }
      }
    }
  }
});

db.createCollection("content", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["title", "author", "contentType"],
      properties: {
        title: {
          bsonType: "string",
          description: "Content title is required"
        },
        contentType: {
          bsonType: "string",
          enum: ["BLOG_POST", "TUTORIAL", "COURSE_SUMMARY", "STUDY_NOTES", "CODE_EXAMPLE"],
          description: "Content type must be one of the defined types"
        },
        status: {
          bsonType: "string",
          enum: ["DRAFT", "PUBLISHED", "ARCHIVED"],
          description: "Content status"
        }
      }
    }
  }
});

db.createCollection("user_progress", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userId"],
      properties: {
        userId: {
          bsonType: "string",
          description: "User ID is required"
        },
        courses: {
          bsonType: "array",
          description: "Array of course progress objects"
        },
        learningPaths: {
          bsonType: "array",
          description: "Array of learning path progress"
        },
        achievements: {
          bsonType: "array",
          description: "User achievements and badges"
        }
      }
    }
  }
});

db.createCollection("learning_paths", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["title", "createdBy"],
      properties: {
        title: {
          bsonType: "string",
          description: "Learning path title is required"
        },
        difficulty: {
          bsonType: "string",
          enum: ["BEGINNER", "INTERMEDIATE", "ADVANCED"],
          description: "Difficulty level"
        },
        estimatedDuration: {
          bsonType: "int",
          minimum: 1,
          description: "Estimated duration in hours"
        }
      }
    }
  }
});

db.createCollection("roles", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name"],
      properties: {
        name: {
          bsonType: "string",
          enum: ["ROLE_USER", "ROLE_ADMIN", "ROLE_INSTRUCTOR"],
          description: "Role name must be one of the defined roles"
        }
      }
    }
  }
});

// Create indexes for optimal performance
print("Creating indexes for SkillMate AI Platform...");

// User indexes
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "firebaseUid": 1 }, { sparse: true });

// Course indexes for search and filtering
db.courses.createIndex({ "title": 1 });
db.courses.createIndex({ "platform": 1 });
db.courses.createIndex({ "qualityScore": -1 });
db.courses.createIndex({ "technologies": 1 });
db.courses.createIndex({ "platform": 1, "qualityScore": -1 });

// Text search indexes
db.courses.createIndex({ 
  "title": "text", 
  "description": "text" 
}, { 
  weights: { 
    "title": 10, 
    "description": 5 
  },
  name: "course_text_search"
});

// Review indexes for analytics
db.reviews.createIndex({ "courseId": 1 });
db.reviews.createIndex({ "userId": 1 });
db.reviews.createIndex({ "overallRating": -1 });
db.reviews.createIndex({ "reviewDate": -1 });
db.reviews.createIndex({ "courseId": 1, "reviewDate": -1 });

// Content indexes
db.content.createIndex({ "author.id": 1 });
db.content.createIndex({ "contentType": 1 });
db.content.createIndex({ "status": 1 });
db.content.createIndex({ "createdAt": -1 });

// User progress indexes for learning analytics
db.user_progress.createIndex({ "userId": 1 }, { unique: true });
db.user_progress.createIndex({ "lastActivityDate": -1 });
db.user_progress.createIndex({ "userId": 1, "lastActivityDate": -1 });

// Learning path indexes
db.learning_paths.createIndex({ "createdBy": 1 });
db.learning_paths.createIndex({ "difficulty": 1 });
db.learning_paths.createIndex({ "estimatedDuration": 1 });

// Role indexes
db.roles.createIndex({ "name": 1 }, { unique: true });

// Insert default roles
db.roles.insertMany([
  { "name": "ROLE_USER" },
  { "name": "ROLE_ADMIN" },
  { "name": "ROLE_INSTRUCTOR" }
]);

// Create sample data for testing (optional)
print("Creating sample data...");

// Sample users
db.users.insertOne({
  "username": "admin",
  "email": "admin@skillmate.ai",
  "password": "$2a$10$encrypted_password_hash",
  "firstName": "Admin",
  "lastName": "User",
  "roles": ["ROLE_ADMIN"],
  "createdAt": new Date(),
  "enabled": true
});

db.users.insertOne({
  "username": "demo_user",
  "email": "demo@skillmate.ai",
  "password": "$2a$10$encrypted_password_hash",
  "firstName": "Demo",
  "lastName": "User",
  "roles": ["ROLE_USER"],
  "createdAt": new Date(),
  "enabled": true
});

// Sample courses
db.courses.insertMany([
  {
    "title": "Complete Machine Learning Course",
    "description": "Comprehensive ML course covering fundamentals to advanced topics",
    "platform": "UDEMY",
    "instructor": "Dr. ML Expert",
    "duration": 40,
    "technologies": ["Python", "TensorFlow", "Scikit-learn"],
    "qualityScore": 95.5,
    "rating": 4.8,
    "enrollments": 15000,
    "price": { "amount": 89.99, "currency": "USD" },
    "createdAt": new Date()
  },
  {
    "title": "React Development Bootcamp",
    "description": "Master React from beginner to advanced level",
    "platform": "COURSERA",
    "instructor": "React Master",
    "duration": 30,
    "technologies": ["JavaScript", "React", "Node.js"],
    "qualityScore": 88.2,
    "rating": 4.6,
    "enrollments": 8500,
    "price": { "amount": 59.99, "currency": "USD" },
    "createdAt": new Date()
  }
]);

print("✅ SkillMate AI Platform database initialized successfully!");
print("📊 Collections created: users, courses, reviews, content, user_progress, learning_paths, roles");
print("🔍 Indexes created for optimal query performance");
print("👤 Default admin user: admin@skillmate.ai");
print("🎓 Sample courses added for testing");
print("");
print("🚀 Database is ready for SkillMate AI Platform!");
