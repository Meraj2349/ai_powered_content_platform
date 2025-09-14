package org.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.example.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Firebase Test Controller
 * Provides endpoints to test Firebase connectivity
 */
@RestController
@RequestMapping("/api/firebase")
public class FirebaseController {

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testFirebase() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean connectionTest = firebaseService.testConnection();
            Map<String, Object> projectInfo = firebaseService.getProjectInfo();

            response.put("connectionTest", connectionTest);
            response.put("projectInfo", projectInfo);
            response.put("status", connectionTest ? "success" : "failed");
            response.put("message", connectionTest ? "Firebase is working properly!" : "Firebase connection failed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Firebase test failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        Map<String, Object> response = firebaseService.getProjectInfo();
        return ResponseEntity.ok(response);
    }
}
