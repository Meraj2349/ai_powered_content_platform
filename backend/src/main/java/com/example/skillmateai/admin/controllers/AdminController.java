package com.example.skillmateai.admin.controllers;

import com.example.skillmateai.admin.dtos.DeleteCoursePathRequest;
import com.example.skillmateai.admin.dtos.DeleteUserRequest;
import com.example.skillmateai.admin.dtos.SearchUserRequest;
import com.example.skillmateai.admin.dtos.ManageAdminPrivilegeRequest;
import com.example.skillmateai.admin.services.AdminService;
import com.example.skillmateai.admin.utilities.CreateAdminResponseUtil;
import com.example.skillmateai.content.entities.CoursePathEntity;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.utilities.MatchTextPatternUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final CreateAdminResponseUtil createAdminResponseUtil;
    private final MatchTextPatternUtil matchTextPatternUtil;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createAdminResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            List<UserEntity> users = adminService.getAllUsers();
            List<Map<String, Object>> userInfoList = createAdminResponseUtil.createUserInfoList(users);
            return ResponseEntity.ok(createAdminResponseUtil.withData(true, "Users fetched successfully", "users", userInfoList, "totalCount", users.size()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createAdminResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error in getAllUsers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createAdminResponseUtil.basic(false, "Internal server error"));
        }
    }

    @GetMapping("/course-paths")
    public ResponseEntity<Map<String, Object>> getAllCoursePaths() {
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createAdminResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            List<CoursePathEntity> coursePaths = adminService.getAllCoursePaths();
            List<Map<String, Object>> coursePathInfoList = createAdminResponseUtil.createCoursePathInfoList(coursePaths);
            return ResponseEntity.ok(createAdminResponseUtil.withData(true, "Course paths fetched successfully", "coursePaths", coursePathInfoList, "totalCount", coursePaths.size()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createAdminResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error in getAllCoursePaths: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createAdminResponseUtil.basic(false, "Internal server error"));
        }
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestBody DeleteUserRequest request) {
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createAdminResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "User ID is required"));
            }

            adminService.deleteUser(request.getUserId());
            return ResponseEntity.ok(createAdminResponseUtil.withData(true, "User deleted successfully", "deletedUserId", request.getUserId()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createAdminResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error in deleteUser: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createAdminResponseUtil.basic(false, "Internal server error"));
        }
    }

    @DeleteMapping("/course-path/delete")
    public ResponseEntity<Map<String, Object>> deleteCoursePath(@RequestBody DeleteCoursePathRequest request) {
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createAdminResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if (request == null || request.getCoursePathId() == null || request.getCoursePathId().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "Course path ID is required"));
            }

            adminService.deleteCoursePath(request.getCoursePathId());
            return ResponseEntity.ok(createAdminResponseUtil.withData(true, "Course path deleted successfully", "deletedCoursePathId", request.getCoursePathId()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createAdminResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error in deleteCoursePath: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createAdminResponseUtil.basic(false, "Internal server error"));
        }
    }

    @PostMapping("/users/search")
    public ResponseEntity<Map<String, Object>> searchUserByEmail(@RequestBody SearchUserRequest request) {
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createAdminResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "Email is required"));
            }

            // Validate email format using MatchTextPatternUtil
            if (!matchTextPatternUtil.isValidEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "Invalid email format"));
            }

            UserEntity user = adminService.searchUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.ok(createAdminResponseUtil.basic(true, "No user found with the provided email"));
            }

            Map<String, Object> userInfo = createAdminResponseUtil.createUserInfoMap(user);
            return ResponseEntity.ok(createAdminResponseUtil.withData(true, "User found", "user", userInfo));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createAdminResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error in searchUserByEmail: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createAdminResponseUtil.basic(false, "Internal server error"));
        }
    }

    @PostMapping("/users/manage-admin-privilege")
    public ResponseEntity<Map<String, Object>> manageAdminPrivilege(@RequestBody ManageAdminPrivilegeRequest request) {
        try {
            // Check user verification
            ResponseEntity<Map<String, Object>> verificationResult = createAdminResponseUtil.validateUserVerification();
            if (verificationResult != null) {
                return verificationResult;
            }
            
            if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "User ID is required"));
            }

            if (request.getAction() == null || request.getAction().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "Action is required (GRANT or REVOKE)"));
            }

            String action = request.getAction().toUpperCase().trim();
            if (!action.equals("GRANT") && !action.equals("REVOKE")) {
                return ResponseEntity.badRequest()
                        .body(createAdminResponseUtil.basic(false, "Action must be either GRANT or REVOKE"));
            }

            UserEntity updatedUser;
            String successMessage;

            if (action.equals("GRANT")) {
                updatedUser = adminService.grantAdminPrivilege(request.getUserId());
                successMessage = "Admin privileges granted successfully";
            } else {
                updatedUser = adminService.revokeAdminPrivilege(request.getUserId());
                successMessage = "Admin privileges revoked successfully";
            }

            Map<String, Object> userInfo = createAdminResponseUtil.createUserInfoMap(updatedUser);
            return ResponseEntity.ok(createAdminResponseUtil.withData(true, successMessage, "user", userInfo));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createAdminResponseUtil.basic(false, e.getReason() == null ? "Request failed" : e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error in manageAdminPrivilege: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createAdminResponseUtil.basic(false, "Internal server error"));
        }
    }
}
