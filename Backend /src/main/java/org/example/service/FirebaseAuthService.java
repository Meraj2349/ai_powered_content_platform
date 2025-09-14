package org.example.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

/**
 * Firebase Authentication Service for SkillMate AI Platform
 * Handles Firebase user management and authentication
 */
@Service
@ConditionalOnProperty(name = "firebase.config.enabled", havingValue = "true")
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    @Value("${firebase.config.enabled:false}")
    private boolean firebaseEnabled;

    /**
     * Test Firebase Authentication connection
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!firebaseEnabled) {
                result.put("status", "⚠️ Firebase is disabled in configuration");
                result.put("enabled", false);
                return result;
            }

            // Try to get Firebase Auth instance to test connection
            FirebaseAuth auth = FirebaseAuth.getInstance();
            result.put("status", "✅ Firebase Authentication Connected Successfully");
            result.put("enabled", true);
            result.put("service", "Firebase Authentication");
            logger.info("Firebase Authentication connection test successful");

        } catch (Exception e) {
            logger.error("Firebase connection test failed: {}", e.getMessage());
            result.put("status", "❌ Firebase Connection Failed");
            result.put("error", e.getMessage());
            result.put("enabled", false);
        }

        return result;
    }

    /**
     * Create user in Firebase Authentication
     */
    public UserRecord createUser(String email, String password, String displayName) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(displayName)
                .setEmailVerified(false) // Users need to verify email
                .setDisabled(false);

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

        logger.info("✅ Successfully created Firebase user: {} with email: {}", userRecord.getUid(), email);

        return userRecord;
    }

    /**
     * Set custom claims for user (roles, permissions)
     */
    public void setCustomClaims(String uid, Map<String, Object> claims) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
        logger.info("✅ Set custom claims for user: {} - {}", uid, claims);
    }

    /**
     * Verify Firebase ID Token
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        logger.info("✅ Verified Firebase token for user: {}", decodedToken.getUid());
        return decodedToken;
    }

    /**
     * Get user by UID
     */
    public UserRecord getUserByUid(String uid) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
        logger.info("Retrieved Firebase user: {}", uid);
        return userRecord;
    }

    /**
     * Get user by email
     */
    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
        logger.info("Retrieved Firebase user by email: {}", email);
        return userRecord;
    }

    /**
     * Update user in Firebase
     */
    public UserRecord updateUser(String uid, String email, String displayName) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setEmail(email)
                .setDisplayName(displayName);

        UserRecord updatedUser = FirebaseAuth.getInstance().updateUser(request);
        logger.info("✅ Updated Firebase user: {}", uid);
        return updatedUser;
    }

    /**
     * Delete user from Firebase
     */
    public void deleteUser(String uid) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        FirebaseAuth.getInstance().deleteUser(uid);
        logger.info("✅ Successfully deleted Firebase user: {}", uid);
    }

    /**
     * Generate custom token for user
     */
    public String createCustomToken(String uid, Map<String, Object> claims) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        String customToken = FirebaseAuth.getInstance().createCustomToken(uid, claims);
        logger.info("✅ Created custom token for user: {}", uid);
        return customToken;
    }

    /**
     * Send email verification
     */
    public String generateEmailVerificationLink(String email) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        String link = FirebaseAuth.getInstance().generateEmailVerificationLink(email);
        logger.info("✅ Generated email verification link for: {}", email);
        return link;
    }

    /**
     * Send password reset email
     */
    public String generatePasswordResetLink(String email) throws FirebaseAuthException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase is not enabled");
        }

        String link = FirebaseAuth.getInstance().generatePasswordResetLink(email);
        logger.info("✅ Generated password reset link for: {}", email);
        return link;
    }

    /**
     * Send password reset email via Firebase
     */
    public void sendPasswordResetEmail(String email) throws FirebaseAuthException {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            // Generate password reset link
            String resetLink = auth.generatePasswordResetLink(email);

            logger.info("✅ Password reset link generated for email: {}", email);
            // Note: In a real application, you would send this link via email service
            // For now, we just log it (Firebase can handle email sending if configured)

        } catch (FirebaseAuthException e) {
            logger.error("❌ Failed to send password reset email: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update user email
     */
    public UserRecord updateUserEmail(String uid, String newEmail) throws FirebaseAuthException {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setEmail(newEmail)
                    .setEmailVerified(false); // Require re-verification

            UserRecord updatedUser = auth.updateUser(request);
            logger.info("✅ Updated user email for UID: {}", uid);
            return updatedUser;

        } catch (FirebaseAuthException e) {
            logger.error("❌ Failed to update user email: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Disable user account
     */
    public UserRecord disableUser(String uid) throws FirebaseAuthException {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setDisabled(true);

            UserRecord updatedUser = auth.updateUser(request);
            logger.info("✅ Disabled user account for UID: {}", uid);
            return updatedUser;

        } catch (FirebaseAuthException e) {
            logger.error("❌ Failed to disable user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if Firebase is enabled
     */
    public boolean isFirebaseEnabled() {
        return firebaseEnabled;
    }

    /**
     * Get Firebase project info
     */
    public Map<String, Object> getProjectInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            info.put("firebase_enabled", firebaseEnabled);
            info.put("service", "Firebase Authentication");
            info.put("features", new String[] {
                    "User Registration",
                    "Email Verification",
                    "Password Reset",
                    "Custom Claims",
                    "Token Verification",
                    "User Management"
            });

            if (firebaseEnabled) {
                info.put("status", "✅ Firebase Authentication Active");
            } else {
                info.put("status", "⚠️ Firebase Authentication Disabled");
            }

        } catch (Exception e) {
            info.put("status", "❌ Firebase Error: " + e.getMessage());
            info.put("error", e.getMessage());
        }

        return info;
    }
}
