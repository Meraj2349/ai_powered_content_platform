package org.example.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

/**
 * Firebase Service
 * Handles Firebase operations
 */
@Service
public class FirebaseService {

    /**
     * Test Firebase connection by writing and reading a test document
     */
    public boolean testConnection() {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // Test document data
            Map<String, Object> testData = new HashMap<>();
            testData.put("message", "Firebase connection test");
            testData.put("timestamp", System.currentTimeMillis());
            testData.put("status", "success");

            // Write test document
            db.collection("test").document("connection-test").set(testData).get();

            // Read test document
            var document = db.collection("test").document("connection-test").get().get();

            if (document.exists()) {
                System.out.println("✅ Firebase Firestore connection test successful!");
                System.out.println("📄 Test document data: " + document.getData());
                return true;
            } else {
                System.err.println("❌ Firebase test document not found");
                return false;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Firebase connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get Firebase project info
     */
    public Map<String, Object> getProjectInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            Firestore db = FirestoreClient.getFirestore();
            info.put("status", "connected");
            info.put("projectId", "aipoweredcontentplatform");
            info.put("database", "firestore");
            info.put("timestamp", System.currentTimeMillis());

            System.out.println("✅ Firebase project info retrieved successfully");
            return info;

        } catch (Exception e) {
            System.err.println("❌ Failed to get Firebase project info: " + e.getMessage());
            info.put("status", "error");
            info.put("error", e.getMessage());
            return info;
        }
    }
}
