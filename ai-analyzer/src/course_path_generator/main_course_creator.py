"""
Main orchestrator file that combines all three modules to create a complete learning course path.

This file integrates:
1. get_topics.py - Generate learning topics using Gemini
2. get_youtube_videos.py - Fetch YouTube videos for each topic
3. create_course_path.py - Analyze videos and create structured course path
"""

import json
import time
from typing import Dict, Any

# Import our custom modules
from src.course_path_generator.get_topics import generate_learning_topics
from src.course_path_generator.get_youtube_videos import get_youtube_videos_for_topics, print_videos_data
from src.course_path_generator.create_course_path import create_course_path, print_course_path


def fetch_and_analyze_topics_individually(topics: list[str], subject: str, difficulty_level: str) -> list[Dict[str, Any]]:
    """
    Fetch videos for each topic and immediately analyze with Gemini.
    
    Args:
        topics (list[str]): List of topics to process
        subject (str): Subject name
        difficulty_level (str): Difficulty level
    
    Returns:
        list[Dict[str, Any]]: List of analyzed topic structures
    """
    
    # Import required modules
    from src.course_path_generator.get_youtube_videos import search_youtube_videos
    from src.course_path_generator.create_course_path import analyze_topic_videos_with_gemini, create_topic_structure
    import os
    import google.generativeai as genai
    
    # Configure Gemini API
    api_key = os.getenv('GEMINI_API_KEY')
    if not api_key:
        raise ValueError("GEMINI_API_KEY not found in environment variables")
    
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-2.0-flash-exp')
    
    # Configure yt-dlp options for video fetching
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'extract_flat': False,
        'writesubtitles': True,
        'writeautomaticsub': True,
        'subtitleslangs': ['en'],
        'skip_download': True,
    }
    
    analyzed_topics = []
    
    for i, topic in enumerate(topics, 1):
        print(f"\n  [{i}/{len(topics)}] Processing: '{topic}'")
        
        try:
            # Step A: Fetch 5 videos for this topic
            print(f"    🎥 Fetching videos...")
            videos_for_topic = search_youtube_videos(topic, ydl_opts, subject)
            print(f"    ✅ Found {len(videos_for_topic)} videos")
            
            if not videos_for_topic:
                print(f"    ⚠️ No videos found for '{topic}', skipping...")
                continue
            
            # Step B: Immediately analyze these videos with Gemini
            print(f"    🧠 Analyzing with Gemini...")
            best_video_analysis = analyze_topic_videos_with_gemini(
                model, topic, videos_for_topic, subject, difficulty_level
            )
            
            if best_video_analysis:
                # Step C: Create topic structure
                topic_structure = create_topic_structure(
                    topic, best_video_analysis, i
                )
                analyzed_topics.append(topic_structure)
                print(f"    ✅ Successfully analyzed '{topic}'")
            else:
                print(f"    ❌ Failed to analyze '{topic}'")
                
        except Exception as e:
            print(f"    ❌ Error processing topic '{topic}': {str(e)}")
            continue
    
    return analyzed_topics


def create_complete_course(subject: str, difficulty_level: str) -> Dict[str, Any]:
   
    
    print("🚀 Starting Complete Course Creation Process")
    print("=" * 60)
    print(f"Subject: {subject}")
    print(f"Difficulty Level: {difficulty_level}")
    print("=" * 60)
    
    try:
        # Step 1: Generate learning topics using Gemini
        print("\n📚 STEP 1: Generating Learning Topics")
        print("-" * 40)
        
        start_time = time.time()
        topics = generate_learning_topics(subject, difficulty_level)
        step1_time = time.time() - start_time
        
        print(f"✅ Generated {len(topics)} topics in {step1_time:.2f} seconds")
        print("Topics generated:")
        for i, topic in enumerate(topics, 1):
            print(f"  {i}. {topic}")
        
        # Step 2: Fetch videos and analyze each topic immediately
        print("\n🎥🧠 STEP 2: Fetching Videos & Analyzing Each Topic")
        print("-" * 50)
        
        start_time = time.time()
        analyzed_topics = fetch_and_analyze_topics_individually(topics, subject, difficulty_level)
        step2_time = time.time() - start_time
        
        total_analyzed = len(analyzed_topics)
        print(f"✅ Fetched and analyzed {total_analyzed} topics in {step2_time:.2f} seconds")
        
        # Create final course path structure
        course_id = f"course-{str(__import__('uuid').uuid4())}"
        course_path = {
            "success": True,
            "data": {
                "coursePath": {
                    "id": course_id,
                    "title": f"{subject} Learning Path",
                    "description": f"A step-by-step learning path for mastering {subject} at {difficulty_level} level.",
                    "targetLevel": difficulty_level.lower()
                },
                "topics": analyzed_topics
            }
        }
        print(f"✅ Analyzed and created course path with {total_analyzed} topics in {step2_time:.2f} seconds")
        
        # Summary
        total_time = step1_time + step2_time
        print("\n🎉 COURSE CREATION COMPLETED!")
        print("=" * 60)
        print(f"Total Time: {total_time:.2f} seconds")
        print(f"Topics Generated: {len(topics)}")
        print(f"Topics Analyzed: {total_analyzed}")
        print("=" * 60)
        
        return course_path
        
    except Exception as e:
        print(f"\n❌ Error in course creation process: {str(e)}")
        return {
            "success": False,
            "error": str(e),
            "data": None
        }


def save_course_to_file(course_path: Dict[str, Any], subject: str, difficulty_level: str) -> str:
    """
    Save the generated course path to a JSON file.
    
    Args:
        course_path (Dict[str, Any]): The complete course path
        subject (str): Subject name for filename
        difficulty_level (str): Difficulty level for filename
    
    Returns:
        str: Path to the saved file
    """
    
    # Create filename
    safe_subject = subject.replace(' ', '_').replace('/', '_').lower()
    filename = f"course_{safe_subject}_{difficulty_level}.json"
    filepath = f"w:/PythonProject1/{filename}"
    
    try:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(course_path, f, indent=2, ensure_ascii=False)
        
        print(f"\n💾 Course saved to: {filepath}")
        return filepath
        
    except Exception as e:
        print(f"\n❌ Error saving course to file: {str(e)}")
        return ""


def interactive_course_creator():
    
    # Get user input
    while True:
        subject = input("\nEnter the subject you want to learn: ").strip()
        if subject:
            break
        print("Please enter a valid subject.")
    
    while True:
        difficulty = input("\nEnter difficulty level (beginner/intermediate/advanced): ").strip().lower()
        if difficulty in ['beginner', 'intermediate', 'advanced']:
            break
        print("Please enter a valid difficulty level: beginner, intermediate, or advanced.")
    
    # Ask if user wants to save the result
    save_option = input("\nDo you want to save the course to a file? (y/n): ").strip().lower()
    should_save = save_option in ['y', 'yes']
    
    # Ask if user wants to see detailed output
    verbose_option = input("\nDo you want to see detailed course information? (y/n): ").strip().lower()
    show_details = verbose_option in ['y', 'yes']
    
    print(f"\n🚀 Creating course for '{subject}' at {difficulty} level...")
    
    # Create the course
    course_path = create_complete_course(subject, difficulty)
    
    # Show results
    if course_path.get('success'):
        if show_details:
            print_course_path(course_path)
        
        if should_save:
            save_course_to_file(course_path, subject, difficulty)
        
        # Show summary
        data = course_path.get('data', {})
        course_info = data.get('coursePath', {})
        topics = data.get('topics', [])
        
        print(f"\n📋 COURSE SUMMARY")
        print("-" * 30)
        print(f"Title: {course_info.get('title')}")
        print(f"Description: {course_info.get('description')}")
        print(f"Total Topics: {len(topics)}")
        print(f"Course ID: {course_info.get('id')}")
        
    else:
        print(f"\n❌ Course creation failed: {course_path.get('error', 'Unknown error')}")



# Main execution
if __name__ == "__main__":

    interactive_course_creator()

    
    print("\n🎉 Thank you for using Complete Course Creator!")
