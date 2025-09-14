package org.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.example.service.FirebaseAuthService;
import org.example.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Firebase Authentication Controller for SkillMate AI Platform
 * Provides endpoints for Firebase authentication testing and management
 */
@RestController
@RequestMapping("/api/firebase-auth")
@CrossOrigin(origins = "*")
public class FirebaseAuthController {

    @Autowired(required = false)
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private FirebaseService firebaseService;

    /**
     * Test Firebase Authentication connection
     * GET /api/firebase-auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testFirebaseAuth() {
        Map<String, Object> result = new HashMap<>();

        if (firebaseAuthService != null) {
            result = firebaseAuthService.testConnection();
        } else {
            result.put("status", "⚠️ Firebase Authentication Service not available");
            result.put("enabled", false);
            result.put("message", "Firebase is disabled or not configured");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get Firebase Authentication status and configuration
     * GET /api/firebase-auth/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFirebaseAuthStatus() {
        Map<String, Object> status = new HashMap<>();

        if (firebaseAuthService != null) {
            status = firebaseAuthService.getProjectInfo();
        } else {
            status.put("service", "Firebase Authentication");
            status.put("status", "⚠️ Firebase Authentication Disabled");
            status.put("enabled", false);
            status.put("reason", "Firebase is not enabled in configuration");
        }

        // Add Firestore status as well
        Map<String, Object> firestoreInfo = firebaseService.getProjectInfo();
        status.put("firestore", firestoreInfo);

        status.put("endpoints", Map.of(
                "test", "/api/firebase-auth/test",
                "status", "/api/firebase-auth/status",
                "signup_firebase_only", "/api/auth/signup-firebase-only",
                "verify_token", "/api/auth/verify-firebase-token"));

        return ResponseEntity.ok(status);
    }

    /**
     * Get comprehensive Firebase integration overview
     * GET /api/firebase-auth/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getFirebaseOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Authentication status
        if (firebaseAuthService != null) {
            overview.put("authentication", firebaseAuthService.getProjectInfo());
        } else {
            overview.put("authentication", Map.of(
                    "status", "⚠️ Disabled",
                    "enabled", false));
        }

        // Firestore status
        overview.put("firestore", firebaseService.getProjectInfo());

        // Integration features
        overview.put("features", Map.of(
                "user_registration", firebaseAuthService != null ? "✅ Available" : "❌ Disabled",
                "email_verification", firebaseAuthService != null ? "✅ Available" : "❌ Disabled",
                "password_reset", firebaseAuthService != null ? "✅ Available" : "❌ Disabled",
                "custom_claims", firebaseAuthService != null ? "✅ Available" : "❌ Disabled",
                "token_verification", firebaseAuthService != null ? "✅ Available" : "❌ Disabled",
                "firestore_database", "✅ Available"));

        // Usage examples
        overview.put("usage", Map.of(
                "signup_with_firebase", "POST /api/auth/signup",
                "signup_firebase_only", "POST /api/auth/signup-firebase-only",
                "verify_firebase_token", "POST /api/auth/verify-firebase-token",
                "test_connection", "GET /api/firebase-auth/test"));

        return ResponseEntity.ok(overview);
    }
}
