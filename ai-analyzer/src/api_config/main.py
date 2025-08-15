"""
Simple FastAPI server for course creation.
Takes subject and difficulty level, returns complete course path.
"""
import os
import sys
from typing import Dict, Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

# Add the parent directory to Python path to access course_path_gen
current_dir = os.path.dirname(__file__)
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

# Now import from course_path_gen
try:
    from course_path_gen.learning_api_service import LearningAPIService
    print("Successfully imported LearningAPIService")
except ImportError as e:
    print(f"Warning: Could not import LearningAPIService: {e}")
    LearningAPIService = None

# Load environment variables from the root directory
load_dotenv(os.path.join(parent_dir, '..', '.env'))

# Initialize FastAPI app
app = FastAPI(
    title="Skill mate ai analyzer API",
    description="API to create complete learning courses with AI-analyzed YouTube videos",
    version="1.0.0"
)

# Configure CORS
cors_origins = os.getenv('CORS_ORIGINS', 'http://localhost:3000,http://localhost:8080').split(',')

app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

# Request model
class CourseRequest(BaseModel):
    subject: str
    difficulty: str
    
    class Config:
        schema_extra = {
            "example": {
                "subject": "fast api",
                "difficulty": "beginner"
            }
        }

# Response model for success
class CourseResponse(BaseModel):
    success: bool
    data: Dict[str, Any] = None
    error: str = None
    message: str = None

@app.get("/")
async def root():
    return {"message": "Skill mate AI Analyzer API", "status": "running", "docs": "/docs"}

@app.get("/api/v1/health")
async def health_check():
    return {"status": "healthy", "service": "course-generator"}

@app.post("/api/v1/generate-course-path", response_model=CourseResponse)
async def create_course_endpoint(request: CourseRequest):
    try:
        if not LearningAPIService:
            raise HTTPException(status_code=500, detail="Learning service not available")
        
        print(f"Generating course for: {request.subject} ({request.difficulty})")
        
        # Initialize the service
        learning_service = LearningAPIService()
        
        # Generate the course
        result = learning_service.generate_course_path(request.subject, request.difficulty)
        
        if result:
            return CourseResponse(
                success=True,
                data=result,
                message="Course generated successfully"
            )
        else:
            return CourseResponse(
                success=False,
                error="Failed to generate course path",
                message="Course generation failed"
            )
            
    except Exception as e:
        print(f"Error generating course: {e}")
        return CourseResponse(
            success=False,
            error=str(e),
            message="Error occurred during course generation"
        )

if __name__ == "__main__":
    import uvicorn
    
    # Get port from environment or use default
    port = int(os.getenv('PORT', 8000))
    host = os.getenv('HOST', '127.0.0.1')
    
    print("=" * 60)
    print("SKILL MATE AI ANALYZER API STARTING...")
    print("=" * 60)
    print(f"Starting Course Creator API on {host}:{port}")
    print(f"CORS Origins: {cors_origins}")
    print("API Documentation: http://127.0.0.1:8000/docs")
    print("=" * 60)
    
    uvicorn.run(
        "main:app",
        host=host, 
        port=port,
        reload=True
    )