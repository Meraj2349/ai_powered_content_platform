package com.example.skillmateai.content.controllers;

import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import com.example.skillmateai.content.services.ProgressService;
import com.example.skillmateai.content.utilities.CreateContentResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/content/course-path")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    @Autowired
    private CreateContentResponseUtil createContentResponseUtil;

    @Autowired
    private ProgressService progressService;

    @GetMapping("/progress/{coursePathId}")
    public ResponseEntity<Map<String,Object>> getUserProgress(@PathVariable String coursePathId){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }

            UserCourseProgressEntity progress = progressService.getUserProgress(coursePathId);
            return ResponseEntity.ok(createContentResponseUtil.withData(true, "Fetched user progress", "progress", progress));
        } catch (org.springframework.web.server.ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(createContentResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e){
            log.error("Error fetching user progress: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createContentResponseUtil.basic(false, "An error occurred while fetching progress"));
        }
    }
}
