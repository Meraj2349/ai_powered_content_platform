// MongoDB initialization script for AI Content Platform

// Switch to the ai_content_platform database
db = db.getSiblingDB('ai_content_platform');

// Create application user
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

// Create collections with validation rules
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['username', 'email', 'password'],
      properties: {
        username: {
          bsonType: 'string',
          minLength: 3,
          maxLength: 50,
          description: 'Username must be a string between 3-50 characters'
        },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
          description: 'Email must be a valid email address'
        },
        password: {
          bsonType: 'string',
          minLength: 6,
          description: 'Password must be at least 6 characters'
        }
      }
    }
  }
});

db.createCollection('content', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['title', 'body', 'contentType', 'author'],
      properties: {
        title: {
          bsonType: 'string',
          minLength: 1,
          maxLength: 200,
          description: 'Title must be a string between 1-200 characters'
        },
        body: {
          bsonType: 'string',
          minLength: 1,
          description: 'Body must be a non-empty string'
        },
        contentType: {
          enum: ['BLOG_POST', 'ARTICLE', 'SOCIAL_MEDIA_POST', 'EMAIL_TEMPLATE', 'PRODUCT_DESCRIPTION'],
          description: 'Content type must be one of the predefined types'
        }
      }
    }
  }
});

db.createCollection('roles');

// Create indexes for better performance
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "firebaseUid": 1 }, { sparse: true });

db.content.createIndex({ "author": 1 });
db.content.createIndex({ "contentType": 1 });
db.content.createIndex({ "tags": 1 });
db.content.createIndex({ "createdAt": -1 });
db.content.createIndex({ "title": "text", "body": "text" });

db.roles.createIndex({ "name": 1 }, { unique: true });

// Insert default roles
db.roles.insertMany([
  {
    name: 'ROLE_USER',
    description: 'Default user role'
  },
  {
    name: 'ROLE_ADMIN',
    description: 'Administrator role'
  },
  {
    name: 'ROLE_MODERATOR',
    description: 'Content moderator role'
  }
]);

print('AI Content Platform database initialized successfully!');
print('Created database: ai_content_platform');
print('Created user: app_user');
print('Created collections: users, content, roles');
print('Created indexes for optimal performance');
print('Inserted default roles');
