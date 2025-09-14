# SkillMate AI Platform - Frontend Integration Guide

## Quick Start

Your frontend is now integrated with your Spring Boot backend! Here are the ways to start your application:

### Option 1: Quick Start (Recommended)

```bash
cd /home/meraj/IdeaProjects/ai_powered_content_platform/backend
./start-with-frontend.sh
```

### Option 2: Manual Maven Build

```bash
cd /home/meraj/IdeaProjects/ai_powered_content_platform/backend
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 3: Rebuild Frontend and Start

```bash
cd /home/meraj/IdeaProjects/ai_powered_content_platform/backend
./build-and-deploy-frontend.sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Access Your Application

Once started, your application will be available at:

- **Frontend**: http://localhost:8080
- **Backend API**: http://localhost:8080/api/\*

## What's Configured

1. ✅ Frontend build files copied to `src/main/resources/static/`
2. ✅ Spring Boot configured to serve static files
3. ✅ React Router support (SPA routing)
4. ✅ CORS configuration for API calls
5. ✅ Automated frontend build in Maven lifecycle
6. ✅ Environment variables setup for all configurations

## Development Workflow

1. **Make frontend changes**: Edit files in `Front-End/skillmate-frontend/src/`
2. **Rebuild and deploy**: Run `./build-and-deploy-frontend.sh`
3. **Restart backend**: The backend will serve the updated frontend

## Important Notes

- Update environment variables in `start-with-frontend.sh` with your actual values
- Configure MongoDB connection details
- Set up proper JWT secrets
- Configure email settings for your SMTP server

## Environment Variables to Configure

Update these in `start-with-frontend.sh`:

- `MONGODB_*`: Your MongoDB connection details
- `MAIL_*`: Your email/SMTP configuration
- `JWT_KEY` & `REFRESH_TOKEN_KEY`: Secure secret keys
- `AI_ANALYZER_BASE_URL`: Your AI service endpoint
