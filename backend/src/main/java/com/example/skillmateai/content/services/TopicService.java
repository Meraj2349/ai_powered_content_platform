package com.example.skillmateai.content.services;

import com.example.skillmateai.content.entities.ProgressEntry;
import com.example.skillmateai.content.entities.TopicEntity;
import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import com.example.skillmateai.content.repositories.CoursePathRepository;
import com.example.skillmateai.content.repositories.TopicRepository;
import com.example.skillmateai.content.repositories.UserCourseProgressRepository;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.repositories.UserRepository;
import com.example.skillmateai.user.utilities.GetAuthenticatedUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final GetAuthenticatedUserUtil getAuthenticatedUserUtil;

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
