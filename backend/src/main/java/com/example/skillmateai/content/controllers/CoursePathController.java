package com.example.skillmateai.content.controllers;


import com.example.skillmateai.content.dtos.GenerateCoursePathRequest;
import com.example.skillmateai.content.dtos.EnrollCoursePathRequest;
import com.example.skillmateai.content.dtos.AddReviewRequest;
import com.example.skillmateai.content.dtos.ToggleTopicStatusRequest;
import com.example.skillmateai.content.entities.CoursePathEntity;
import com.example.skillmateai.content.entities.TopicEntity;
import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import com.example.skillmateai.content.services.CoursePathService;
import com.example.skillmateai.content.utilities.CreateContentResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/content/course-path")
@RequiredArgsConstructor
@Slf4j
public class CoursePathController {

    private final CoursePathService coursePathService;
    private final CreateContentResponseUtil createContentResponseUtil;

    @PostMapping("/generate")
    public ResponseEntity<Map<String,Object>> generateCoursePath(@RequestBody GenerateCoursePathRequest request){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(request == null){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Request body is required"));
            }
            if(request.getSubject() == null || request.getSubject().isBlank() || request.getDifficulty() == null || request.getDifficulty().isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Subject and difficulty are required"));
            }
            Map<String,Object> data = coursePathService.generateAndPersistCoursePath(request.getSubject(), request.getDifficulty());
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Course path generated", "data", data));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error generating course path: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while generating course path"));
        }
    }

//    @PostMapping("/generate/public")
//    public ResponseEntity<Map<String,Object>> generateCoursePathPublic(@RequestBody GenerateCoursePathRequest request){
//        try {
//            if(request == null){
//                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Request body is required"));
//            }
//            Map<String,Object> data = coursePathService.generateCoursePathNoAuth(request.getSubject(), request.getDifficulty());
//            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Course path generated", "data", data));
//        } catch (org.springframework.web.server.ResponseStatusException e){
//            return ResponseEntity.status(e.getStatusCode())
//                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
//        } catch (Exception e){
//            log.error("Error generating public course path: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while generating course path"));
//        }
//    }

    @GetMapping("/mine")
    public ResponseEntity<Map<String,Object>> getMyCoursePaths(){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            List<CoursePathEntity> list = coursePathService.getMyCoursePaths();
            List<Map<String,Object>> summaryList = createContentResponseUtil.createCoursePathSummaryList(list);
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Fetched course paths", "coursePaths", summaryList));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error fetching course paths: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while fetching course paths"));
        }
    }

    @PostMapping("/enroll")
    public ResponseEntity<Map<String,Object>> enrollInCoursePath(@RequestBody EnrollCoursePathRequest request){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(request == null || request.getCoursePathId() == null || request.getCoursePathId().isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Course path ID is required"));
            }
            Map<String,Object> data = coursePathService.enrollInCoursePath(request.getCoursePathId());
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Successfully enrolled in course path", "data", data));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error enrolling in course path: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while enrolling"));
        }
    }

    @GetMapping("/progress/{coursePathId}")
    public ResponseEntity<Map<String,Object>> getUserProgress(@PathVariable String coursePathId){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            UserCourseProgressEntity progress = coursePathService.getUserProgress(coursePathId);
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Fetched user progress", "progress", progress));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error fetching user progress: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while fetching progress"));
        }
    }

    @PostMapping("/generate/check-duplicate")
    public ResponseEntity<Map<String,Object>> generateCoursePathWithDuplicateCheck(@RequestBody GenerateCoursePathRequest request){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(request == null){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Request body is required"));
            }
            if(request.getSubject() == null || request.getSubject().isBlank() || request.getDifficulty() == null || request.getDifficulty().isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Subject and difficulty are required"));
            }
            Map<String,Object> data = coursePathService.generateAndPersistCoursePathWithDuplicateCheck(request.getSubject(), request.getDifficulty());
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Course path processed", "data", data));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error generating course path with duplicate check: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while generating course path"));
        }





    }

    @GetMapping("/search")
    public ResponseEntity<Map<String,Object>> searchCoursePaths(@RequestParam String query){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(query == null || query.isEmpty()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Search query is required"));
            }
            List<CoursePathEntity> results = coursePathService.searchSimilarCoursePaths(query);
            List<Map<String,Object>> searchResults = createContentResponseUtil.createCoursePathSearchResultList(results);
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Search completed", "coursePaths", searchResults));
        } catch (Exception e){
            log.error("Error searching course paths: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while searching"));
        }
    }

    @PostMapping("/review")
    public ResponseEntity<Map<String,Object>> addReview(@RequestBody AddReviewRequest request){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(request == null){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Request body is required"));
            }
            if(request.getCoursePathId() == null || request.getCoursePathId().isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Course path ID is required"));
            }
            if(request.getRating() == null || request.getRating() < 1 || request.getRating() > 5){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Rating must be between 1 and 5"));
            }
            Map<String,Object> data = coursePathService.addReviewToCoursePath(request.getCoursePathId(), request.getRating(), request.getComment());
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Review added successfully", "data", data));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error adding review: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while adding review"));
        }
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<Map<String,Object>> getTopicInfo(@PathVariable String topicId){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            TopicEntity topic = coursePathService.getTopicById(topicId);
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Topic fetched successfully", "topic", topic));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error fetching topic: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while fetching topic"));
        }
    }

    @PostMapping("progress/toggle-topic-status")
    public ResponseEntity<Map<String,Object>> toggleTopicCoveredStatus(@RequestBody ToggleTopicStatusRequest request){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(request == null){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Request body is required"));
            }
            if(request.getProgressId() == null || request.getProgressId().isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Progress ID is required"));
            }
            if(request.getTopicId() == null || request.getTopicId().isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Topic ID is required"));
            }
            Map<String,Object> data = coursePathService.toggleTopicCoveredStatus(request.getProgressId(), request.getTopicId());
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Topic status toggled successfully", "data", data));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error toggling topic status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while toggling topic status"));
        }
    }
}
