package org.example.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.example.dto.request.LoginRequest;
import org.example.dto.request.SignUpRequest;
import org.example.dto.response.ApiResponse;
import org.example.model.Role;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtTokenProvider;
import org.example.service.FirebaseAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.UserRecord;

import jakarta.validation.Valid;

/**
 * Enhanced Authentication Controller with Firebase Integration
 * Provides secure user authentication, registration, and Firebase integration
 * for SkillMate AI Platform with comprehensive validation and logging
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Password complexity pattern: min 8 chars, 1 uppercase, 1 lowercase, 1 digit,
    // 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired(required = false)
    private FirebaseAuthService firebaseAuthService;

    @Value("${firebase.config.enabled:false}")
    private boolean firebaseEnabled;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Authentication successful");
            response.put("accessToken", jwt);
            response.put("tokenType", "Bearer");

            logger.info("User {} authenticated successfully", loginRequest.getUsernameOrEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("🔥 Registration attempt for user: {} with email: {}",
                signUpRequest.getUsername(), signUpRequest.getEmail());

        try {
            // Enhanced input validation
            Map<String, String> validationErrors = validateSignUpRequest(signUpRequest);
            if (!validationErrors.isEmpty()) {
                logger.warn("❌ Validation failed for user: {} - {}",
                        signUpRequest.getUsername(), validationErrors);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Validation failed", "errors", validationErrors));
            }

            // Check if user already exists
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("❌ Username already exists: {}", signUpRequest.getUsername());
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username is already taken!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("❌ Email already exists: {}", signUpRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email Address already in use!"));
            }

            // Validate password complexity
            if (!PASSWORD_PATTERN.matcher(signUpRequest.getPassword()).matches()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Password must be at least 8 characters long, " +
                                "and include uppercase, lowercase, number, and special character"));
            }

            // Validate email format
            if (!EMAIL_PATTERN.matcher(signUpRequest.getEmail()).matches()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid email format"));
            }

            // Create user in Firebase first (if enabled)
            String firebaseUid = null;
            if (firebaseEnabled && firebaseAuthService != null) {
                try {
                    UserRecord firebaseUser = firebaseAuthService.createUser(
                            signUpRequest.getEmail(),
                            signUpRequest.getPassword(),
                            signUpRequest.getFirstName() + " " + signUpRequest.getLastName());
                    firebaseUid = firebaseUser.getUid();

                    // Set custom claims for role-based access
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("role", "USER");
                    claims.put("platform", "SkillMate-AI");
                    claims.put("username", signUpRequest.getUsername());
                    firebaseAuthService.setCustomClaims(firebaseUid, claims);

                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse(false, "Failed to create Firebase user: " + e.getMessage()));
                }
            }

            // Create user in MongoDB
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword());

            user.setFirstName(signUpRequest.getFirstName());
            user.setLastName(signUpRequest.getLastName());
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Set Firebase UID if created
            if (firebaseUid != null) {
                user.setFirebaseUid(firebaseUid);
                user.setFirebaseVerified(true);
            }

            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User Role not set."));

            user.setRoles(Collections.singleton(userRole));

            User result = userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully! Please check your email for verification.");
            response.put("userId", result.getId());
            response.put("firebaseEnabled", firebaseEnabled);
            response.put("firebaseUid", firebaseUid);
            response.put("email", result.getEmail());
            response.put("username", result.getUsername());

            logger.info("User {} registered successfully", result.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signup-firebase-only")
    public ResponseEntity<?> registerFirebaseUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (!firebaseEnabled || firebaseAuthService == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Firebase is not enabled"));
        }

        try {
            // Create user only in Firebase (useful for testing Firebase integration)
            UserRecord firebaseUser = firebaseAuthService.createUser(
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword(),
                    signUpRequest.getFirstName() + " " + signUpRequest.getLastName());

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "USER");
            claims.put("platform", "SkillMate-AI");
            claims.put("username", signUpRequest.getUsername());
            firebaseAuthService.setCustomClaims(firebaseUser.getUid(), claims);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Firebase user created successfully");
            response.put("firebaseUid", firebaseUser.getUid());
            response.put("email", firebaseUser.getEmail());
            response.put("displayName", firebaseUser.getDisplayName());
            response.put("emailVerified", firebaseUser.isEmailVerified());

            logger.info("Firebase user {} created successfully", firebaseUser.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Firebase user creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Firebase user creation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-firebase-token")
    public ResponseEntity<?> verifyFirebaseToken(@RequestBody Map<String, String> request) {
        if (!firebaseEnabled || firebaseAuthService == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Firebase is not enabled"));
        }

        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "ID token is required"));
            }

            var decodedToken = firebaseAuthService.verifyIdToken(idToken);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token verified successfully");
            response.put("uid", decodedToken.getUid());
            response.put("email", decodedToken.getEmail());
            response.put("name", decodedToken.getName());
            response.put("emailVerified", decodedToken.isEmailVerified());

            logger.info("Firebase token verified successfully for UID: {}", decodedToken.getUid());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Token verification failed: " + e.getMessage()));
        }
    }

    /**
     * Enhanced input validation for user registration
     */
    private Map<String, String> validateSignUpRequest(SignUpRequest signUpRequest) {
        Map<String, String> errors = new HashMap<>();

        // Email validation
        if (!EMAIL_PATTERN.matcher(signUpRequest.getEmail()).matches()) {
            errors.put("email", "Please provide a valid email address");
        }

        // Password complexity validation
        if (!PASSWORD_PATTERN.matcher(signUpRequest.getPassword()).matches()) {
            errors.put("password",
                    "Password must be at least 8 characters long and contain uppercase, lowercase, digit and special character");
        }

        // Username validation
        if (signUpRequest.getUsername().length() < 3 || signUpRequest.getUsername().length() > 20) {
            errors.put("username", "Username must be between 3 and 20 characters");
        }

        // Name validation
        if (signUpRequest.getFirstName() == null || signUpRequest.getFirstName().trim().isEmpty()) {
            errors.put("firstName", "First name is required");
        }

        if (signUpRequest.getLastName() == null || signUpRequest.getLastName().trim().isEmpty()) {
            errors.put("lastName", "Last name is required");
        }

        return errors;
    }

    /**
     * User authentication endpoint with enhanced security
     */

    /**
     * Password reset endpoint
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email is required"));
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Please provide a valid email address"));
        }

        logger.info("🔄 Password reset requested for email: {}", email);

        try {
            // Check if user exists
            if (!userRepository.existsByEmail(email)) {
                // Don't reveal if email exists for security
                logger.warn("❌ Password reset attempted for non-existent email: {}", email);
            }

            // Try Firebase password reset if enabled
            if (firebaseEnabled && firebaseAuthService != null) {
                firebaseAuthService.sendPasswordResetEmail(email);
                logger.info("✅ Firebase password reset email sent to: {}", email);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "If an account with that email exists, a password reset link has been sent.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Password reset failed for email: {} - {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Password reset failed. Please try again later."));
        }
    }

    /**
     * User profile endpoint
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required"));
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("firebaseUid", user.getFirebaseUid());
            response.put("firebaseVerified", user.isFirebaseVerified());
            response.put("roles", user.getRoles());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve user profile"));
        }
    }
}
