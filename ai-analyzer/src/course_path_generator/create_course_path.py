import os
import json
import uuid
from typing import List, Dict, Any
from dotenv import load_dotenv
import google.generativeai as genai

# Load environment variables
load_dotenv()

def create_course_path(videos_data: List[Dict[str, Any]], subject: str, difficulty_level: str) -> Dict[str, Any]:
    """
    Analyze YouTube videos for each topic and create a structured course path using Gemini.
    
    Args:
        videos_data (List[Dict[str, Any]]): Array of topic dictionaries from get_youtube_videos_for_topics
        subject (str): The subject name (e.g., "Python Programming", "Machine Learning")
        difficulty_level (str): The difficulty level ("beginner", "intermediate", "advanced")
    
    Returns:
        Dict[str, Any]: Structured course path with analyzed video recommendations
    """
    
    # Configure Gemini API
    api_key = os.getenv('GEMINI_API_KEY')
    if not api_key:
        raise ValueError("GEMINI_API_KEY not found in environment variables")
    
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-2.0-flash-exp')
    
    print(f"Creating course path for: {subject} ({difficulty_level} level)")
    print(f"Analyzing {len(videos_data)} topics...")
    
    # Generate course path ID and basic info
    course_id = f"course-{str(uuid.uuid4())}"
    
    analyzed_topics = []
    
    for i, topic_data in enumerate(videos_data, 1):
        topic_name = topic_data['topic_name']
        videos = topic_data['videos']
        
        print(f"\n[{i}/{len(videos_data)}] Analyzing topic: '{topic_name}'")
        
        if not videos:
            print(f"  ⚠️ No videos found for topic '{topic_name}', skipping...")
            continue
        
        try:
            # Analyze this topic's videos with Gemini
            best_video_analysis = analyze_topic_videos_with_gemini(
                model, topic_name, videos, subject, difficulty_level
            )
            
            if best_video_analysis:
                # Create topic structure
                topic_structure = create_topic_structure(
                    topic_name, best_video_analysis, i
                )
                analyzed_topics.append(topic_structure)
                print(f"  ✅ Successfully analyzed '{topic_name}'")
            else:
                print(f"  ❌ Failed to analyze '{topic_name}'")
                
        except Exception as e:
            print(f"  ❌ Error analyzing topic '{topic_name}': {str(e)}")
            continue
    
    # Create final course path structure
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
    
    print(f"\n🎉 Course path created successfully!")
    print(f"Total topics analyzed: {len(analyzed_topics)}")
    
    return course_path


def analyze_topic_videos_with_gemini(model, topic_name: str, videos: List[Dict], subject: str, difficulty_level: str) -> Dict[str, Any]:
    """
    Use Gemini to analyze videos for a specific topic and recommend the best one.
    
    Args:
        model: Gemini model instance
        topic_name (str): Name of the topic
        videos (List[Dict]): List of video dictionaries
        subject (str): Subject name
        difficulty_level (str): Difficulty level
    
    Returns:
        Dict[str, Any]: Analysis result with best video recommendation
    """
    
    # Prepare video information for Gemini
    videos_info = []
    for i, video in enumerate(videos, 1):
        video_info = f"""
Video {i}:
Title: {video.get('title', 'N/A')}
URL: {video.get('url', 'N/A')}
Description: {video.get('description', 'N/A')}
Subtitles: {video.get('subtitles', 'N/A')}
Views: {video.get('view_count', 0):,}
Likes: {video.get('like_count', 0):,}
Duration: {video.get('duration', 0)} seconds
Channel: {video.get('channel', 'N/A')}
"""
        videos_info.append(video_info)
    
    # Create comprehensive prompt for Gemini
    prompt = f"""You are an expert educational content curator. Analyze these 5 YouTube videos for the topic "{topic_name}" in the subject "{subject}" at {difficulty_level} level.

TASK: Select the BEST video and provide specific start/end times for the most relevant content.

ANALYSIS CRITERIA (in priority order):
1. PRIMARY: Content quality based on subtitles - analyze if the spoken content matches the topic and difficulty level
2. SECONDARY: Video metrics (views, likes) as supporting indicators
3. Look for timestamp information in descriptions to identify relevant segments
4. Ensure content is appropriate for {difficulty_level} learners

VIDEOS TO ANALYZE:
{chr(10).join(videos_info)}

INSTRUCTIONS:
- Analyze the subtitle content to determine which video has the highest quality explanation for "{topic_name}"
- Look for timestamps in descriptions that indicate relevant sections
- If no specific timestamps are mentioned, analyze the entire video duration
- Consider the {difficulty_level} level - content should not be too basic or too advanced
- Focus on educational value over popularity metrics

REQUIRED OUTPUT FORMAT (JSON only, no other text):
{{
  "selectedVideo": {{
    "videoNumber": 1,
    "youtubeUrl": "https://www.youtube.com/watch?v=...",
    "title": "Video Title",
    "reason": "Why this video was selected (max 200 chars)",
    "startTimeMs": 0,
    "endTimeMs": 300000,
    "contentQuality": "high|medium|low",
    "relevanceScore": 95
  }}
}}

Respond with ONLY the JSON object, no additional text."""

    try:
        # Call Gemini API
        response = model.generate_content(prompt)
        response_text = response.text.strip()
        
        # Parse JSON response
        if response_text.startswith('```json'):
            response_text = response_text[7:-3].strip()
        elif response_text.startswith('```'):
            response_text = response_text[3:-3].strip()
        
        analysis_result = json.loads(response_text)
        return analysis_result
        
    except json.JSONDecodeError as e:
        print(f"    Error parsing Gemini response as JSON: {e}")
        print(f"    Raw response: {response_text[:200]}...")
        return None
    except Exception as e:
        print(f"    Error calling Gemini API: {e}")
        return None


def create_topic_structure(topic_name: str, analysis: Dict[str, Any], index: int) -> Dict[str, Any]:
    """
    Create the final topic structure from Gemini analysis.
    
    Args:
        topic_name (str): Name of the topic
        analysis (Dict[str, Any]): Analysis result from Gemini
        index (int): Topic index for ordering
    
    Returns:
        Dict[str, Any]: Structured topic data
    """
    
    selected_video = analysis.get('selectedVideo', {})
    
    # Generate topic ID
    topic_id = f"topic-{str(uuid.uuid4())}"
    
    # Convert milliseconds to seconds for start/end times
    start_time_ms = selected_video.get('startTimeMs', 0)
    end_time_ms = selected_video.get('endTimeMs', 0)
    start_time_sec = int(start_time_ms / 1000) if start_time_ms else 0
    end_time_sec = int(end_time_ms / 1000) if end_time_ms else 0
    
    # Generate prerequisites (topics depend on previous ones)
    prerequisites = []
    if index > 1:
        # For now, just make each topic depend on the previous one
        # In a real implementation, you might want more sophisticated dependency analysis
        prerequisites = [f"previous-topic-{index-1}"]
    
    # Generate tags from topic name
    tags = generate_tags_from_topic(topic_name)
    
    topic_structure = {
        "id": topic_id,
        "name": topic_name,
        "description": f"Learn about {topic_name.lower()} - {selected_video.get('reason', 'Educational content')}",
        "videoInfo": {
            "youtubeUrl": selected_video.get('youtubeUrl', ''),
            "title": selected_video.get('title', ''),
            "startTime": start_time_sec,
            "endTime": end_time_sec
        },
        "prerequisites": prerequisites,
        "tags": tags,
        "qualityMetrics": {
            "contentQuality": selected_video.get('contentQuality', 'medium'),
            "relevanceScore": selected_video.get('relevanceScore', 0)
        }
    }
    
    return topic_structure


def generate_tags_from_topic(topic_name: str) -> List[str]:
    """
    Generate relevant tags from topic name.
    
    Args:
        topic_name (str): Name of the topic
    
    Returns:
        List[str]: List of relevant tags
    """
    
    # Simple tag generation - extract key words
    import re
    
    # Convert to lowercase and extract words
    words = re.findall(r'\b\w+\b', topic_name.lower())
    
    # Filter out common words
    common_words = {'and', 'or', 'the', 'a', 'an', 'in', 'on', 'at', 'to', 'for', 'of', 'with', 'by'}
    tags = [word for word in words if word not in common_words and len(word) > 2]
    
    # Limit to 5 tags
    return tags[:5]


def print_course_path(course_path: Dict[str, Any]) -> None:
    """
    Print the course path in a readable format.
    
    Args:
        course_path (Dict[str, Any]): The structured course path
    """
    
    if not course_path.get('success'):
        print("❌ Course path creation failed")
        return
    
    data = course_path.get('data', {})
    course_info = data.get('coursePath', {})
    topics = data.get('topics', [])
    
    print("\n" + "="*80)
    print("GENERATED COURSE PATH")
    print("="*80)
    
    print(f"Course ID: {course_info.get('id')}")
    print(f"Title: {course_info.get('title')}")
    print(f"Description: {course_info.get('description')}")
    print(f"Target Level: {course_info.get('targetLevel')}")
    print(f"Total Topics: {len(topics)}")
    
    for i, topic in enumerate(topics, 1):
        print(f"\n{'-'*60}")
        print(f"TOPIC {i}: {topic.get('name')}")
        print(f"{'-'*60}")
        print(f"ID: {topic.get('id')}")
        print(f"Description: {topic.get('description')}")
        
        video_info = topic.get('videoInfo', {})
        print(f"Video URL: {video_info.get('youtubeUrl')}")
        print(f"Video Title: {video_info.get('title')}")
        print(f"Start Time: {video_info.get('startTime')} seconds")
        print(f"End Time: {video_info.get('endTime')} seconds")
        
        quality_metrics = topic.get('qualityMetrics', {})
        print(f"Content Quality: {quality_metrics.get('contentQuality')}")
        print(f"Relevance Score: {quality_metrics.get('relevanceScore')}")
        
        print(f"Prerequisites: {', '.join(topic.get('prerequisites', [])) if topic.get('prerequisites') else 'None'}")
        print(f"Tags: {', '.join(topic.get('tags', []))}")


# Example usage
def test_course_path_creation():
    """
    Test function - requires videos_data from get_youtube_videos_for_topics
    """
    # This would normally be called with real data from get_youtube_videos_for_topics
    print("To test this function, call it with real videos_data:")
    print("course_path = create_course_path(videos_data, 'Python Programming', 'beginner')")
    print("print_course_path(course_path)")


if __name__ == "__main__":
    test_course_path_creation()
