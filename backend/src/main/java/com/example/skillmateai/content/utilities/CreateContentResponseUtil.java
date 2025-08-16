package com.example.skillmateai.content.utilities;

import com.example.skillmateai.content.entities.CoursePathEntity;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.utilities.GetAuthenticatedUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CreateContentResponseUtil {

    @Autowired
    private GetAuthenticatedUserUtil getAuthenticatedUserUtil;

    /**
     * Validates that the user is authenticated and verified
     * @return ResponseEntity with error message if user is not authenticated or verified, null if validation passes
     */
    public ResponseEntity<Map<String, Object>> validateUserVerification() {
        UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
        
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(basic(false, "User not authenticated"));
        }
        
        if (!user.isVerified()) {
            return ResponseEntity.status(403)
                    .body(basic(false, "User account is not verified. Please verify your email address"));
        }
        
        return null; // Validation passed
    }

    public Map<String,Object> basic(boolean success, String message){
        Map<String,Object> m = new HashMap<>();
        m.put("success", success);
        m.put("message", message);
        return m;
    }

    public Map<String,Object> withData(boolean success, String message, String dataName, Object data){
        Map<String,Object> m = basic(success, message);
        m.put(dataName, data);
        return m;
    }

    public Map<String,Object> withData(boolean success, String message, String dataName1, Object data1, String dataName2, Object data2){
        Map<String,Object> m = withData(success, message, dataName1, data1);
        m.put(dataName2, data2);
        return m;
    }
    
    public Map<String,Object> withMultipleData(boolean success, String message, String dataName1, Object data1, String dataName2, Object data2, String dataName3, Object data3){
        Map<String,Object> m = withData(success, message, dataName1, data1, dataName2, data2);
        m.put(dataName3, data3);
        return m;
    }

    public Map<String,Object> createCoursePathSummaryMap(CoursePathEntity coursePath) {
        try {
            Map<String, Object> summaryMap = new HashMap<>();
            summaryMap.put("id", coursePath.getId());
            summaryMap.put("title", coursePath.getTitle());
            summaryMap.put("description", coursePath.getDescription());
            summaryMap.put("topicCount", coursePath.getTopics() != null ? coursePath.getTopics().size() : 0);
            return summaryMap;

        } catch (Exception e) {
            log.error("Error creating course path summary map: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String,Object>> createCoursePathSummaryList(List<CoursePathEntity> coursePaths) {
        try {
            List<Map<String,Object>> summaryList = new ArrayList<>();
            for(CoursePathEntity coursePath : coursePaths) {
                Map<String,Object> summary = createCoursePathSummaryMap(coursePath);
                if(summary != null) {
                    summaryList.add(summary);
                }
            }
            return summaryList;

        } catch (Exception e) {
            log.error("Error creating course path summary list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String,Object> createCoursePathSearchResultMap(CoursePathEntity coursePath) {
        try {
            Map<String, Object> searchResultMap = new HashMap<>();
            searchResultMap.put("id", coursePath.getId());
            searchResultMap.put("title", coursePath.getTitle());
            searchResultMap.put("topicCount", coursePath.getTopics() != null ? coursePath.getTopics().size() : 0);
            searchResultMap.put("createdAt", coursePath.getCreatedAt());
            return searchResultMap;

        } catch (Exception e) {
            log.error("Error creating course path search result map: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String,Object>> createCoursePathSearchResultList(List<CoursePathEntity> coursePaths) {
        try {
            List<Map<String,Object>> searchResultList = new ArrayList<>();
            for(CoursePathEntity coursePath : coursePaths) {
                Map<String,Object> searchResult = createCoursePathSearchResultMap(coursePath);
                if(searchResult != null) {
                    searchResultList.add(searchResult);
                }
            }
            return searchResultList;

        } catch (Exception e) {
            log.error("Error creating course path search result list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
