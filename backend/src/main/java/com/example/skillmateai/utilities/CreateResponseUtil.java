package com.example.skillmateai.utilities;

import com.example.skillmateai.entities.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CreateResponseUtil {

    @Autowired
    private GetAuthenticatedUserUtil getAuthenticatedUserUtil;

    public Map createResponseBody(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }


    public Map createResponseBody(boolean success, String message, String dataName, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put(dataName, data);
        return response;
    }

    public Map createResponseBody(boolean success, String message, String dataName1, Object data1, String dataName2, Object data2, String dataName3, Object data3) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put(dataName1, data1);
        response.put(dataName2, data2);
        response.put(dataName3, data3);
        return response;
    }

    public Map createMap(String dataName, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put(dataName, data);
        return response;
    }

    public Map createMap(String dataName, ArrayList data) {
        Map<String, Object> response = new HashMap<>();
        response.put(dataName, data);
        return response;
    }

    public Map createUserInfoMap(UserEntity user) {
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




}
