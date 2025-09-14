#!/bin/bash

# MongoDB Setup Script for SkillMate AI Platform
# This script initializes the MongoDB database with collections, indexes, and sample data

echo "🚀 Starting MongoDB setup for SkillMate AI Platform..."

# Check if MongoDB is running
if ! pgrep -x "mongod" > /dev/null; then
    echo "❌ MongoDB is not running. Please start MongoDB first:"
    echo "   sudo systemctl start mongod"
    echo "   # or"
    echo "   brew services start mongodb-community"
    exit 1
fi

echo "✅ MongoDB is running"

# Check if mongosh is available
if command -v mongosh &> /dev/null; then
    MONGO_CMD="mongosh"
elif command -v mongo &> /dev/null; then
    MONGO_CMD="mongo"
else
    echo "❌ Neither mongosh nor mongo command found. Please install MongoDB tools."
    exit 1
fi

echo "📦 Using MongoDB command: $MONGO_CMD"

# Create database and setup collections
echo "🗄️ Creating database and collections..."

$MONGO_CMD --eval "
// Switch to SkillMate AI database
db = db.getSiblingDB('ai_content_platform');

print('📊 Creating SkillMate AI Platform database...');

// Create application user with read/write permissions
try {
  db.createUser({
    user: 'app_user',
    pwd: 'app_password123',
    roles: [
      {
        role: 'readWrite',
        db: 'ai_content_platform'
      }
    ]
  });
  print('✅ Created database user: app_user');
} catch (e) {
  if (e.code === 51003) {
    print('⚠️ User app_user already exists');
  } else {
    print('❌ Error creating user: ' + e.message);
  }
}

// Create collections with validation schemas
print('📋 Creating collections with validation...');

// Users collection
try {
  db.createCollection('users', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['username', 'email', 'password'],
        properties: {
          username: {
            bsonType: 'string',
            description: 'Username must be a string and is required'
          },
          email: {
            bsonType: 'string',
            pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\$',
            description: 'Email must be valid and is required'
          },
          password: {
            bsonType: 'string',
            minLength: 8,
            description: 'Password must be at least 8 characters'
          },
          firebaseUid: {
            bsonType: 'string',
            description: 'Firebase user ID for authentication sync'
          },
          firebaseVerified: {
            bsonType: 'bool',
            description: 'Firebase email verification status'
          }
        }
      }
    }
  });
  print('✅ Created users collection');
} catch (e) {
  print('⚠️ Users collection may already exist: ' + e.message);
}

// Courses collection
try {
  db.createCollection('courses', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['title', 'platform'],
        properties: {
          title: {
            bsonType: 'string',
            description: 'Course title is required'
          },
          platform: {
            bsonType: 'string',
            enum: ['UDEMY', 'COURSERA', 'EDX', 'YOUTUBE', 'GITHUB', 'OTHER'],
            description: 'Platform must be one of the supported platforms'
          },
          qualityScore: {
            bsonType: 'double',
            minimum: 0,
            maximum: 100,
            description: 'Quality score between 0 and 100'
          },
          technologies: {
            bsonType: 'array',
            description: 'List of technologies covered in the course'
          }
        }
      }
    }
  });
  print('✅ Created courses collection');
} catch (e) {
  print('⚠️ Courses collection may already exist: ' + e.message);
}

// Reviews collection
try {
  db.createCollection('reviews', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['courseId', 'userId', 'overallRating'],
        properties: {
          courseId: {
            bsonType: 'string',
            description: 'Course ID is required'
          },
          userId: {
            bsonType: 'string',
            description: 'User ID is required'
          },
          overallRating: {
            bsonType: 'int',
            minimum: 1,
            maximum: 5,
            description: 'Rating must be between 1 and 5'
          },
          reviewText: {
            bsonType: 'string',
            description: 'Review text content'
          },
          sentimentAnalysis: {
            bsonType: 'object',
            description: 'AI-generated sentiment analysis results'
          }
        }
      }
    }
  });
  print('✅ Created reviews collection');
} catch (e) {
  print('⚠️ Reviews collection may already exist: ' + e.message);
}

// Content collection
try {
  db.createCollection('content', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['title', 'contentType'],
        properties: {
          title: {
            bsonType: 'string',
            description: 'Content title is required'
          },
          contentType: {
            bsonType: 'string',
            enum: ['BLOG_POST', 'TUTORIAL', 'COURSE_SUMMARY', 'STUDY_NOTES', 'CODE_EXAMPLE'],
            description: 'Content type must be one of the defined types'
          },
          status: {
            bsonType: 'string',
            enum: ['DRAFT', 'PUBLISHED', 'ARCHIVED'],
            description: 'Content status'
          }
        }
      }
    }
  });
  print('✅ Created content collection');
} catch (e) {
  print('⚠️ Content collection may already exist: ' + e.message);
}

// User progress collection
try {
  db.createCollection('user_progress', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['userId'],
        properties: {
          userId: {
            bsonType: 'string',
            description: 'User ID is required'
          },
          courses: {
            bsonType: 'array',
            description: 'Array of course progress objects'
          },
          learningPaths: {
            bsonType: 'array',
            description: 'Array of learning path progress'
          },
          achievements: {
            bsonType: 'array',
            description: 'User achievements and badges'
          }
        }
      }
    }
  });
  print('✅ Created user_progress collection');
} catch (e) {
  print('⚠️ User_progress collection may already exist: ' + e.message);
}

// Learning paths collection
try {
  db.createCollection('learning_paths', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['title', 'createdBy'],
        properties: {
          title: {
            bsonType: 'string',
            description: 'Learning path title is required'
          },
          difficulty: {
            bsonType: 'string',
            enum: ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'],
            description: 'Difficulty level'
          },
          estimatedDuration: {
            bsonType: 'int',
            minimum: 1,
            description: 'Estimated duration in hours'
          }
        }
      }
    }
  });
  print('✅ Created learning_paths collection');
} catch (e) {
  print('⚠️ Learning_paths collection may already exist: ' + e.message);
}

// Roles collection
try {
  db.createCollection('roles', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['name'],
        properties: {
          name: {
            bsonType: 'string',
            enum: ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR'],
            description: 'Role name must be one of the defined roles'
          }
        }
      }
    }
  });
  print('✅ Created roles collection');
} catch (e) {
  print('⚠️ Roles collection may already exist: ' + e.message);
}

print('🔍 Creating indexes for optimal performance...');

// User indexes
try {
  db.users.createIndex({ 'username': 1 }, { unique: true });
  db.users.createIndex({ 'email': 1 }, { unique: true });
  db.users.createIndex({ 'firebaseUid': 1 }, { sparse: true });
  print('✅ Created user indexes');
} catch (e) {
  print('⚠️ Some user indexes may already exist');
}

// Course indexes
try {
  db.courses.createIndex({ 'title': 1 });
  db.courses.createIndex({ 'platform': 1 });
  db.courses.createIndex({ 'qualityScore': -1 });
  db.courses.createIndex({ 'technologies': 1 });
  db.courses.createIndex({ 'platform': 1, 'qualityScore': -1 });
  print('✅ Created course indexes');
} catch (e) {
  print('⚠️ Some course indexes may already exist');
}

// Text search indexes
try {
  db.courses.createIndex({ 
    'title': 'text', 
    'description': 'text' 
  }, { 
    weights: { 
      'title': 10, 
      'description': 5 
    },
    name: 'course_text_search'
  });
  print('✅ Created text search indexes');
} catch (e) {
  print('⚠️ Text search indexes may already exist');
}

// Review indexes
try {
  db.reviews.createIndex({ 'courseId': 1 });
  db.reviews.createIndex({ 'userId': 1 });
  db.reviews.createIndex({ 'overallRating': -1 });
  db.reviews.createIndex({ 'reviewDate': -1 });
  db.reviews.createIndex({ 'courseId': 1, 'reviewDate': -1 });
  print('✅ Created review indexes');
} catch (e) {
  print('⚠️ Some review indexes may already exist');
}

// Content indexes
try {
  db.content.createIndex({ 'author.id': 1 });
  db.content.createIndex({ 'contentType': 1 });
  db.content.createIndex({ 'status': 1 });
  db.content.createIndex({ 'createdAt': -1 });
  print('✅ Created content indexes');
} catch (e) {
  print('⚠️ Some content indexes may already exist');
}

// User progress indexes
try {
  db.user_progress.createIndex({ 'userId': 1 }, { unique: true });
  db.user_progress.createIndex({ 'lastActivityDate': -1 });
  db.user_progress.createIndex({ 'userId': 1, 'lastActivityDate': -1 });
  print('✅ Created user progress indexes');
} catch (e) {
  print('⚠️ Some user progress indexes may already exist');
}

// Learning path indexes
try {
  db.learning_paths.createIndex({ 'createdBy': 1 });
  db.learning_paths.createIndex({ 'difficulty': 1 });
  db.learning_paths.createIndex({ 'estimatedDuration': 1 });
  print('✅ Created learning path indexes');
} catch (e) {
  print('⚠️ Some learning path indexes may already exist');
}

// Role indexes
try {
  db.roles.createIndex({ 'name': 1 }, { unique: true });
  print('✅ Created role indexes');
} catch (e) {
  print('⚠️ Role indexes may already exist');
}

print('👥 Inserting default roles...');

// Insert default roles
try {
  db.roles.insertMany([
    { 'name': 'ROLE_USER' },
    { 'name': 'ROLE_ADMIN' },
    { 'name': 'ROLE_INSTRUCTOR' }
  ]);
  print('✅ Created default roles');
} catch (e) {
  print('⚠️ Default roles may already exist');
}

print('📚 Creating sample data for testing...');

// Sample courses
try {
  db.courses.insertMany([
    {
      'title': 'Complete Machine Learning Course',
      'description': 'Comprehensive ML course covering fundamentals to advanced topics',
      'platform': 'UDEMY',
      'instructor': 'Dr. ML Expert',
      'duration': 40,
      'technologies': ['Python', 'TensorFlow', 'Scikit-learn'],
      'qualityScore': 95.5,
      'rating': 4.8,
      'enrollments': 15000,
      'price': { 'amount': 89.99, 'currency': 'USD' },
      'createdAt': new Date()
    },
    {
      'title': 'React Development Bootcamp',
      'description': 'Master React from beginner to advanced level',
      'platform': 'COURSERA',
      'instructor': 'React Master',
      'duration': 30,
      'technologies': ['JavaScript', 'React', 'Node.js'],
      'qualityScore': 88.2,
      'rating': 4.6,
      'enrollments': 8500,
      'price': { 'amount': 59.99, 'currency': 'USD' },
      'createdAt': new Date()
    }
  ]);
  print('✅ Created sample courses');
} catch (e) {
  print('⚠️ Sample courses may already exist');
}

print('');
print('🎉 SkillMate AI Platform database initialized successfully!');
print('📊 Collections: users, courses, reviews, content, user_progress, learning_paths, roles');
print('🔍 Indexes: Created for optimal query performance');
print('🎓 Sample data: Added for testing');
print('');
print('🚀 Database is ready for SkillMate AI Platform!');
print('');
print('📋 Next steps:');
print('   1. Update application.properties with MongoDB connection details');
print('   2. Start your Spring Boot application');
print('   3. Test the connection with: curl http://localhost:8082/actuator/health');
"

echo ""
echo "✅ MongoDB setup completed successfully!"
echo ""
echo "📋 Database Details:"
echo "   Database: ai_content_platform"
echo "   User: app_user"
echo "   Password: app_password123"
echo ""
echo "🔧 To connect from your application:"
echo "   spring.data.mongodb.database=ai_content_platform"
echo "   spring.data.mongodb.username=app_user"
echo "   spring.data.mongodb.password=app_password123"
echo ""
echo "🧪 Test connection:"
echo "   mongosh ai_content_platform -u app_user -p app_password123"
