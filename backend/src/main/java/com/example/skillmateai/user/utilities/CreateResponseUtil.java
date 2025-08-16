package com.example.skillmateai.user.utilities;

import com.example.skillmateai.user.entities.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CreateResponseUtil {

    public Map<String,Object> createResponseBody(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }


    public Map<String,Object> createResponseBody(boolean success, String message, String dataName, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put(dataName, data);
        return response;
    }

    public Map<String,Object> createResponseBody(boolean success, String message, String dataName1, Object data1, String dataName2, Object data2, String dataName3, Object data3) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put(dataName1, data1);
        response.put(dataName2, data2);
        response.put(dataName3, data3);
        return response;
    }

    public Map<String,Object> createMap(String dataName, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put(dataName, data);
        return response;
    }

    public Map<String,Object> createMap(String dataName, ArrayList<?> data) {
        Map<String, Object> response = new HashMap<>();
        response.put(dataName, data);
        return response;
    }

    public Map<String,Object> createUserInfoMap(UserEntity user) {
        try {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email", user.getEmail());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("isVerified", user.isVerified());
            userMap.put("isAccountEnabled", user.isAccountEnabled());
            userMap.put("isBlocked", user.getIsBlocked());
            return userMap;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public Map<String,Object> createExtendedUserInfoMap(UserEntity user) {
        try {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email", user.getEmail());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("isVerified", user.isVerified());
            userMap.put("isAccountEnabled", user.isAccountEnabled());
            userMap.put("isBlocked", user.getIsBlocked());
            userMap.put("enrolledCoursePaths", user.getEnrolledCoursePaths() != null ? user.getEnrolledCoursePaths() : new ArrayList<>());
            userMap.put("createdCoursePaths", user.getCreatedCoursePaths() != null ? user.getCreatedCoursePaths() : new ArrayList<>());
            return userMap;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }




}
