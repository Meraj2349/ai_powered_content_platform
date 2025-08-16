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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/content/course-path")
@RequiredArgsConstructor
@Slf4j
public class CoursePathController {

    @Autowired
    private CoursePathService coursePathService;

    @Autowired
    private CreateContentResponseUtil createContentResponseUtil;

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



    @GetMapping("/mine")
    public ResponseEntity<Map<String,Object>> getMyCoursePaths(){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            Map<String,Object> coursePathsData = coursePathService.getMyCoursePaths();
            
            // Extract created and enrolled course paths
            @SuppressWarnings("unchecked")
            List<CoursePathEntity> createdCoursePaths = (List<CoursePathEntity>) coursePathsData.get("createdCoursePaths");
            @SuppressWarnings("unchecked") 
            List<CoursePathEntity> enrolledCoursePaths = (List<CoursePathEntity>) coursePathsData.get("enrolledCoursePaths");
            
            // Create summary lists for both
            List<Map<String,Object>> createdSummaryList = createContentResponseUtil.createCoursePathSummaryList(createdCoursePaths);
            List<Map<String,Object>> enrolledSummaryList = createContentResponseUtil.createCoursePathSummaryList(enrolledCoursePaths);
            
            // Build response with both lists
            Map<String,Object> responseData = new HashMap<>();
            responseData.put("createdCoursePaths", createdSummaryList);
            responseData.put("enrolledCoursePaths", enrolledSummaryList);
            
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Fetched user course paths", "userCoursePaths", responseData));
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

    // EXPERIMENTAL - DISABLED: Generate course path with duplicate check endpoint
    // This endpoint is currently experimental and not recommended for production use
    /*
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
    */

    // EXPERIMENTAL - DISABLED: Search course paths endpoint
    // This endpoint is currently experimental and not recommended for production use
    /*
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
    */

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

    @GetMapping("/{coursePathId}")
    public ResponseEntity<Map<String,Object>> getCoursePathById(@PathVariable String coursePathId){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if(coursePathId == null || coursePathId.isBlank()){
                return ResponseEntity.badRequest().body(createContentResponseUtil.basic(false, "Course path ID is required"));
            }
            
            CoursePathEntity coursePath = coursePathService.getCoursePathById(coursePathId);
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Course path fetched successfully", "coursePath", coursePath));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error fetching course path: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while fetching course path"));
        }
    }


}
