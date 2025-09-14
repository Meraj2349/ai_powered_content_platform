# 🎓 SkillMate AI Platform

An AI-powered educational content platform that revolutionizes online learning by aggregating courses from multiple platforms, providing personalized learning paths, and offering comprehensive progress tracking.

## 🚀 Features

### 🤖 AI-Powered Content Generation
- **GPT-4.1 Integration** via Router AI
- **Intelligent Course Recommendations**
- **Automated Study Notes Generation**
- **Personalized Learning Paths**

### 📚 Multi-Platform Course Aggregation
- **Udemy, Coursera, edX Integration**
- **YouTube Educational Content**
- **GitHub Repository Analysis**
- **Quality-based Course Ranking**

### 🔥 Firebase Authentication
- **Social Login Support**
- **Real-time User Sync**
- **Secure Token Management**
- **Cross-platform Authentication**

### 📊 Advanced Analytics
- **Learning Progress Tracking**
- **Sentiment Analysis of Reviews**
- **Performance Metrics**
- **Achievement System**

### 🌐 GitHub Integration
- **Educational Repository Discovery**
- **Trending Technology Analysis**
- **Code Example Integration**
- **Developer Learning Paths**

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Backend** | Spring Boot 3.2.5, Java 17 |
| **Database** | MongoDB 7.0 |
| **Authentication** | JWT + Firebase Auth |
| **AI Integration** | OpenRouter AI (GPT-4.1) |
| **API Integration** | GitHub API, Multiple Course Platforms |
| **Security** | Spring Security, CORS, Rate Limiting |
| **Documentation** | Swagger/OpenAPI 3 |

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │    MongoDB      │
│   (React/Vue)   │◄──►│   Backend API   │◄──►│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │
                               ▼
                    ┌─────────────────┐
                    │  External APIs  │
                    │ • Firebase Auth │
                    │ • Router AI     │
                    │ • GitHub API    │
                    │ • Course APIs   │
                    └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- Firebase Project
- Router AI API Key
- GitHub API Token

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/skillmate-ai-platform.git
   cd skillmate-ai-platform
   ```

2. **Configure environment**
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   # Edit application.properties with your API keys
   ```

3. **Set up Firebase**
   - Create Firebase project
   - Download service account JSON
   - Place in `src/main/resources/firebase-service-account.json`

4. **Install dependencies**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**
   - API: http://localhost:8082/api
   - Health Check: http://localhost:8082/actuator/health
   - API Documentation: http://localhost:8082/swagger-ui.html

## 📡 API Endpoints

### Authentication
```http
POST /api/auth/signup              # User registration
POST /api/auth/signin              # User login
POST /api/auth/signup-firebase-only # Firebase-only registration
POST /api/auth/verify-firebase-token # Firebase token verification
```

### AI Content Generation
```http
POST /api/content/generate         # Generate AI content
GET  /api/content                  # List user content
GET  /api/content/{id}             # Get specific content
PUT  /api/content/{id}/publish     # Publish content
DELETE /api/content/{id}           # Delete content
```

### GitHub Integration
```http
GET /api/github/test               # Test GitHub connection
GET /api/github/education          # Educational repositories
GET /api/github/search             # Repository search
GET /api/github/repo/{owner}/{repo} # Repository details
GET /api/github/trending           # Trending repositories
```

### Firebase Integration
```http
GET /api/firebase-auth/test        # Test Firebase connection
GET /api/firebase-auth/status      # Firebase status
GET /api/firebase-auth/overview    # Integration overview
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### API Testing with curl
```bash
# Health check
curl http://localhost:8082/actuator/health

# User registration
curl -X POST http://localhost:8082/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","username":"johndoe","email":"john@example.com","password":"password123"}'

# AI content generation (requires JWT token)
curl -X POST http://localhost:8082/api/content/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"topic":"Machine Learning","contentType":"BLOG_POST","targetAudience":"BEGINNER"}'
```

## 📊 Project Structure

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── controller/          # REST Controllers
│   │   ├── service/             # Business Logic
│   │   ├── repository/          # Data Access Layer
│   │   ├── model/               # Entity Models
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── config/              # Configuration Classes
│   │   └── security/            # Security Components
│   └── resources/
│       ├── application.properties
│       └── firebase-service-account.json
└── test/                        # Unit Tests
```

## 🔧 Configuration

### Environment Variables
```properties
# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=ai_content_platform

# Router AI
openai.api.key=your-router-ai-key
openai.api.url=https://openrouter.ai/api/v1
openai.model=gpt-4-1106-preview

# GitHub API
github.api.token=your-github-token
github.api.url=https://api.github.com

# Firebase
firebase.config.enabled=true
firebase.project.id=your-firebase-project-id
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **OpenRouter AI** for GPT-4.1 integration
- **Firebase** for authentication services
- **GitHub** for repository data access
- **MongoDB** for flexible data storage
- **Spring Boot** for robust backend framework

## 📞 Support

For support and questions:
- 📧 Email: support@skillmate-ai.com
- 💬 Discord: [SkillMate AI Community](https://discord.gg/skillmate-ai)
- 📖 Documentation: [docs.skillmate-ai.com](https://docs.skillmate-ai.com)

---

**Built with ❤️ for the future of education**