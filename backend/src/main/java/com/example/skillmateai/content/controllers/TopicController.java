package com.example.skillmateai.content.controllers;

import com.example.skillmateai.content.dtos.ToggleTopicStatusRequest;
import com.example.skillmateai.content.entities.TopicEntity;
import com.example.skillmateai.content.services.TopicService;
import com.example.skillmateai.content.utilities.CreateContentResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/content/course-path")
@RequiredArgsConstructor
@Slf4j
public class TopicController {

    @Autowired
    private CreateContentResponseUtil createContentResponseUtil;

    @Autowired
    private TopicService topicService;

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<Map<String,Object>> getTopicInfo(@PathVariable String topicId){
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createContentResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }

            TopicEntity topic = topicService.getTopicById(topicId);
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
            Map<String,Object> data = topicService.toggleTopicCoveredStatus(request.getProgressId(), request.getTopicId());
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
