package com.example.skillmateai.content.services;

import com.example.skillmateai.content.entities.CoursePathEntity;
import com.example.skillmateai.content.entities.TopicEntity;
import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import com.example.skillmateai.content.entities.ProgressEntry;
import com.example.skillmateai.content.entities.ReviewEntity;
import com.example.skillmateai.content.repositories.CoursePathRepository;
import com.example.skillmateai.content.repositories.TopicRepository;
import com.example.skillmateai.content.repositories.UserCourseProgressRepository;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.repositories.UserRepository;
import com.example.skillmateai.user.utilities.GetAuthenticatedUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePathService {

    private final CoursePathRepository coursePathRepository;
    private final TopicRepository topicRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final GetAuthenticatedUserUtil getAuthenticatedUserUtil;
    private final UserRepository userRepository;

    @Value("${ai.analyzer.base-url:http://localhost:8000}")
    private String aiAnalyzerBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String,Object> callAnalyzer(String subject, String difficulty){
        Map<String, Object> requestBody = Map.of("subject", subject, "difficulty", difficulty.toLowerCase());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        Map<String, Object> aiResponse;
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(aiAnalyzerBaseUrl + "/api/v1/generate-course-path", entity, Map.class);
            @SuppressWarnings("unchecked") Map<String,Object> body = response.getBody();
            aiResponse = body;
        } catch (RestClientException e){
            log.error("AI analyzer call failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI Analyzer unreachable");
        }
        if(aiResponse == null || !(Boolean.TRUE.equals(aiResponse.get("success")))){
            log.error("AI analyzer returned unsuccessful response: {}", aiResponse);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate course path");
        }
        return aiResponse;
    }

    public Map<String,Object> generateCoursePathNoAuth(String subject, String difficulty){
        if(subject == null || subject.isBlank() || difficulty == null || difficulty.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject and difficulty are required");
        }
        Map<String,Object> aiResponse = callAnalyzer(subject, difficulty);
        @SuppressWarnings("unchecked") Map<String,Object> data = (Map<String, Object>) aiResponse.get("data");
        return data; // raw coursePath + topics, no persistence
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> generateAndPersistCoursePath(String subject, String difficulty){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            if(subject == null || subject.isBlank() || difficulty == null || difficulty.isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject and difficulty are required");
            }

            Map<String,Object> aiResponse = callAnalyzer(subject, difficulty);
            Map<String, Object> data = (Map<String, Object>) aiResponse.get("data");
            if(data == null){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Malformed AI response");
            }
            Map<String, Object> coursePathMap = (Map<String, Object>) data.get("coursePath");
            List<Map<String, Object>> topicMaps = (List<Map<String, Object>>) data.get("topics");

            if(coursePathMap == null || topicMaps == null){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Incomplete AI response");
            }

            // Persist topics first
            List<TopicEntity> topicEntities = new ArrayList<>();
            for(Map<String, Object> t : topicMaps){
                Map<String, Object> videoInfo = (Map<String, Object>) t.get("videoInfo");
                TopicEntity topic = TopicEntity.builder()
                        .id((String) t.get("id"))
                        .name((String) t.get("name"))
                        .description((String) t.getOrDefault("description", ""))
                        .videoInfo(videoInfo == null ? null : com.example.skillmateai.content.entities.VideoInfo.builder()
                                .youtubeUrl((String) videoInfo.get("youtubeUrl"))
                                .title((String) videoInfo.get("title"))
                                .startTime(((Number) videoInfo.getOrDefault("startTime",0)).intValue())
                                .endTime(((Number) videoInfo.getOrDefault("endTime",0)).intValue())
                                .build())
                        .prerequisites((List<String>) t.getOrDefault("prerequisites", new ArrayList<>()))
                        .estimatedTimeMin(null)
                        .tags((List<String>) t.getOrDefault("tags", new ArrayList<>()))
                        .build();
                topicEntities.add(topic);
            }
            topicRepository.saveAll(topicEntities);

            CoursePathEntity coursePath = CoursePathEntity.builder()
                    .id((String) coursePathMap.get("id"))
                    .creatorId(user.getId())
                    .title((String) coursePathMap.get("title"))
                    .description((String) coursePathMap.get("description"))
                    .targetLevel((String) coursePathMap.get("targetLevel"))
                    .createdAt(System.currentTimeMillis())
                    .createdBy(user.getEmail())
                    .topics(topicEntities.stream().map(TopicEntity::getId).collect(Collectors.toList()))
                    .reviews(new ArrayList<>())
                    .averageRating(0.0)
                    .build();
            coursePathRepository.save(coursePath);

            List<ProgressEntry> progressEntries = topicEntities.stream().map(te -> ProgressEntry.builder()
                    .topicId(te.getId())
                    .isCovered(false)
                    .lastUpdated(System.currentTimeMillis())
                    .build()).collect(Collectors.toList());

            UserCourseProgressEntity progress = UserCourseProgressEntity.builder()
                    .userId(user.getId())
                    .coursePathId(coursePath.getId())
                    .startedAt(System.currentTimeMillis())
                    .readiness(0)
                    .progress(progressEntries)
                    .build();
            userCourseProgressRepository.save(progress);

            Map<String,Object> response = new HashMap<>();
            response.put("coursePath", coursePath);
            response.put("topicsCount", topicEntities.size());
            response.put("progressId", progress.getId());
            return response;
        } catch (ResponseStatusException e){
            throw e; // bubble up for controller to format
        } catch (Exception e){
            log.error("Unexpected error generating course path: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error generating course path");
        }
    }

    public List<CoursePathEntity> getMyCoursePaths(){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            return coursePathRepository.findByCreatorId(user.getId());
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error fetching user course paths: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error fetching course paths");
        }
    }

    public Map<String,Object> enrollInCoursePath(String coursePathId){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            // Check if course path exists
            CoursePathEntity coursePath = coursePathRepository.findById(coursePathId).orElse(null);
            if(coursePath == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course path not found");
            }
            
            // Check if user already enrolled
            if(userCourseProgressRepository.findByUserIdAndCoursePathId(user.getId(), coursePathId).isPresent()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already enrolled in this course path");
            }
            
            // Create progress entries for all topics
            List<ProgressEntry> progressEntries = coursePath.getTopics().stream().map(topicId -> ProgressEntry.builder()
                    .topicId(topicId)
                    .isCovered(false)
                    .lastUpdated(System.currentTimeMillis())
                    .build()).collect(Collectors.toList());

            // Create user progress
            UserCourseProgressEntity progress = UserCourseProgressEntity.builder()
                    .userId(user.getId())
                    .coursePathId(coursePathId)
                    .startedAt(System.currentTimeMillis())
                    .readiness(0)
                    .progress(progressEntries)
                    .build();
            userCourseProgressRepository.save(progress);
            
            // Update user's enrolled course paths
            if(user.getEnrolledCoursePaths() == null){
                user.setEnrolledCoursePaths(new ArrayList<>());
            }
            user.getEnrolledCoursePaths().add(coursePathId);
            userRepository.save(user);
            
            Map<String,Object> response = new HashMap<>();
            response.put("progressId", progress.getId());
            response.put("coursePath", coursePath);
            return response;
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error enrolling in course path: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error enrolling in course path");
        }
    }

    public UserCourseProgressEntity getUserProgress(String coursePathId){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            return userCourseProgressRepository.findByUserIdAndCoursePathId(user.getId(), coursePathId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No progress found for this course path"));
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error fetching user progress: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error fetching progress");
        }
    }

    public Map<String,Object> generateAndPersistCoursePathWithDuplicateCheck(String subject, String difficulty){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            if(subject == null || subject.isBlank() || difficulty == null || difficulty.isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject and difficulty are required");
            }

            // Check for existing similar course paths
            List<CoursePathEntity> existingCourses = searchSimilarCoursePaths(subject + " " + difficulty);
            if(!existingCourses.isEmpty()){
                Map<String,Object> response = new HashMap<>();
                response.put("existingCourse", existingCourses.get(0));
                response.put("message", "Similar course path already exists");
                return response;
            }

            // Generate new course path
            return generateAndPersistCoursePath(subject, difficulty);
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error generating course path with duplicate check: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error generating course path");
        }
    }

    public List<CoursePathEntity> searchSimilarCoursePaths(String query){
        try {
            if(query == null || query.isBlank()){
                return new ArrayList<>();
            }
            
            // Clean and prepare search terms
            String cleanQuery = query.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
            String[] terms = cleanQuery.split("\\s+");
            
            // Build regex pattern for fuzzy matching
            StringBuilder regexBuilder = new StringBuilder();
            for(int i = 0; i < terms.length; i++){
                if(i > 0) regexBuilder.append(".*");
                regexBuilder.append(terms[i]);
            }
            String regexPattern = ".*" + regexBuilder.toString() + ".*";
            
            return coursePathRepository.findByTitleRegex(regexPattern);
        } catch (Exception e){
            log.error("Unexpected error searching course paths: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String,Object> addReviewToCoursePath(String coursePathId, Integer rating, String comment){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            if(rating == null || rating < 1 || rating > 5){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
            }
            
            CoursePathEntity coursePath = coursePathRepository.findById(coursePathId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course path not found"));
            
            // Check if user already reviewed
            if(coursePath.getReviews() != null && coursePath.getReviews().stream()
                    .anyMatch(review -> review.getReviewerId().equals(user.getId()))){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already reviewed this course path");
            }
            
            // Create review
            ReviewEntity review = ReviewEntity.builder()
                    .reviewerId(user.getId())
                    .reviewerName(user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""))
                    .rating(rating)
                    .comment(comment != null ? comment : "")
                    .reviewDate(System.currentTimeMillis())
                    .build();
            
            // Add review to course path
            if(coursePath.getReviews() == null){
                coursePath.setReviews(new ArrayList<>());
            }
            coursePath.getReviews().add(review);
            
            // Calculate new average rating
            double avgRating = coursePath.getReviews().stream()
                    .mapToInt(ReviewEntity::getRating)
                    .average()
                    .orElse(0.0);
            coursePath.setAverageRating(avgRating);
            
            coursePathRepository.save(coursePath);
            
            Map<String,Object> response = new HashMap<>();
            response.put("review", review);
            response.put("newAverageRating", avgRating);
            response.put("totalReviews", coursePath.getReviews().size());
            return response;
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error adding review: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error adding review");
        }
    }

    public TopicEntity getTopicById(String topicId){
        try {
            return topicRepository.findById(topicId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error fetching topic: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error fetching topic");
        }
    }

    public Map<String,Object> toggleTopicCoveredStatus(String progressId, String topicId){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            // Find the progress entity
            UserCourseProgressEntity progress = userCourseProgressRepository.findById(progressId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progress not found"));
            
            // Verify that this progress belongs to the authenticated user
            if(!progress.getUserId().equals(user.getId())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this progress");
            }
            
            // Find the specific topic in the progress
            ProgressEntry targetEntry = null;
            for(ProgressEntry entry : progress.getProgress()){
                if(entry.getTopicId().equals(topicId)){
                    targetEntry = entry;
                    break;
                }
            }
            
            if(targetEntry == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found in this progress");
            }
            
            // Toggle the covered status
            boolean previousStatus = targetEntry.isCovered();
            targetEntry.setCovered(!previousStatus);
            targetEntry.setLastUpdated(System.currentTimeMillis());
            
            // Calculate new readiness percentage
            long coveredCount = progress.getProgress().stream().mapToLong(entry -> entry.isCovered() ? 1 : 0).sum();
            int newReadiness = (int) ((coveredCount * 100) / progress.getProgress().size());
            progress.setReadiness(newReadiness);
            
            userCourseProgressRepository.save(progress);
            
            String action = !previousStatus ? "marked as covered" : "unmarked";
            Map<String,Object> response = new HashMap<>();
            response.put("topicId", topicId);
            response.put("progressId", progressId);
            response.put("action", action);
            response.put("isCovered", !previousStatus);
            response.put("newReadiness", newReadiness);
            response.put("coveredTopics", coveredCount);
            response.put("totalTopics", progress.getProgress().size());
            return response;
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error toggling topic covered status: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error toggling topic covered status");
        }
    }
}
