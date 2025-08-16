package com.example.skillmateai.admin.utilities;

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
public class CreateAdminResponseUtil {

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
        Map<String,Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }

    public Map<String,Object> withData(boolean success, String message, String dataName, Object data){
        Map<String,Object> response = basic(success, message);
        response.put(dataName, data);
        return response;
    }

    public Map<String,Object> withData(boolean success, String message, String dataName1, Object data1, String dataName2, Object data2){
        Map<String,Object> response = withData(success, message, dataName1, data1);
        response.put(dataName2, data2);
        return response;
    }

    public Map<String,Object> createUserInfoMap(UserEntity user) {
        try {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("roles", user.getRoles() != null ? user.getRoles() : new ArrayList<>());
            userMap.put("isAdmin", user.getRoles() != null && user.getRoles().contains("ADMIN"));
            return userMap;

        } catch (Exception e) {
            log.error("Error creating user info map: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String,Object>> createUserInfoList(List<UserEntity> users) {
        try {
            List<Map<String,Object>> userInfoList = new ArrayList<>();
            for(UserEntity user : users) {
                Map<String,Object> userInfo = createUserInfoMap(user);
                if(userInfo != null) {
                    userInfoList.add(userInfo);
                }
            }
            return userInfoList;

        } catch (Exception e) {
            log.error("Error creating user info list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String,Object> createCoursePathInfoMap(CoursePathEntity coursePath) {
        try {
            Map<String, Object> coursePathMap = new HashMap<>();
            coursePathMap.put("id", coursePath.getId());
            coursePathMap.put("title", coursePath.getTitle());
            coursePathMap.put("description", coursePath.getDescription());
            coursePathMap.put("targetLevel", coursePath.getTargetLevel());
            coursePathMap.put("topicCount", coursePath.getTopics() != null ? coursePath.getTopics().size() : 0);
            coursePathMap.put("createdAt", coursePath.getCreatedAt());
            coursePathMap.put("createdBy", coursePath.getCreatedBy());
            coursePathMap.put("averageRating", coursePath.getAverageRating());
            return coursePathMap;

        } catch (Exception e) {
            log.error("Error creating course path info map: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String,Object>> createCoursePathInfoList(List<CoursePathEntity> coursePaths) {
        try {
            List<Map<String,Object>> coursePathInfoList = new ArrayList<>();
            for(CoursePathEntity coursePath : coursePaths) {
                Map<String,Object> coursePathInfo = createCoursePathInfoMap(coursePath);
                if(coursePathInfo != null) {
                    coursePathInfoList.add(coursePathInfo);
                }
            }
            return coursePathInfoList;

        } catch (Exception e) {
            log.error("Error creating course path info list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
