package org.example.config;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * Firebase Configuration
 * Initializes Firebase Admin SDK
 */
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.config.path:}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            System.out.println("🔥 Firebase is disabled in configuration");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Load Firebase service account from resources
                InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId("skillmateaiplatform")
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase Admin SDK initialized successfully!");
            }
        } catch (IOException e) {
            System.out.println("⚠️ Firebase service account file not found: " + e.getMessage());
            // Create a basic Firebase app without credentials for development
            try {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseOptions basicOptions = FirebaseOptions.builder()
                            .setProjectId("skillmateaiplatform")
                            .build();
                    FirebaseApp.initializeApp(basicOptions);
                    System.out.println("🔧 Firebase initialized with basic configuration");
                }
            } catch (Exception basicEx) {
                System.out.println("⚠️ Could not initialize Firebase: " + basicEx.getMessage());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Firebase initialization failed: " + e.getMessage());
        }
    }

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            return FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            System.out.println("⚠️ Firebase not initialized, returning null");
            return null;
        }
    }
}
