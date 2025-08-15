package com.example.skillmateai.admin.services;

import com.example.skillmateai.content.entities.CoursePathEntity;
import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import com.example.skillmateai.content.repositories.CoursePathRepository;
import com.example.skillmateai.content.repositories.TopicRepository;
import com.example.skillmateai.content.repositories.UserCourseProgressRepository;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final CoursePathRepository coursePathRepository;
    private final TopicRepository topicRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;

    public List<UserEntity> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Unexpected error fetching all users: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error fetching users");
        }
    }

    public List<CoursePathEntity> getAllCoursePaths() {
        try {
            return coursePathRepository.findAll();
        } catch (Exception e) {
            log.error("Unexpected error fetching all course paths: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error fetching course paths");
        }
    }

    public void deleteUser(String userId) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
            }

            // Check if user exists
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Delete user's progress records
            List<UserCourseProgressEntity> userProgress = userCourseProgressRepository.findByUserId(userId);
            if (!userProgress.isEmpty()) {
                userCourseProgressRepository.deleteAll(userProgress);
                log.info("Deleted {} progress records for user {}", userProgress.size(), userId);
            }

            // Delete course paths created by user
            List<CoursePathEntity> userCoursePaths = coursePathRepository.findByCreatorId(userId);
            if (!userCoursePaths.isEmpty()) {
                // Delete topics associated with user's course paths
                for (CoursePathEntity coursePath : userCoursePaths) {
                    if (coursePath.getTopics() != null && !coursePath.getTopics().isEmpty()) {
                        topicRepository.deleteAllById(coursePath.getTopics());
                    }
                }
                coursePathRepository.deleteAll(userCoursePaths);
                log.info("Deleted {} course paths created by user {}", userCoursePaths.size(), userId);
            }

            // Finally delete the user
            userRepository.delete(user);
            log.info("Successfully deleted user with ID: {}", userId);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting user {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error deleting user");
        }
    }

    public void deleteCoursePath(String coursePathId) {
        try {
            if (coursePathId == null || coursePathId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course path ID is required");
            }

            // Check if course path exists
            CoursePathEntity coursePath = coursePathRepository.findById(coursePathId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course path not found"));

            // Delete associated topics
            if (coursePath.getTopics() != null && !coursePath.getTopics().isEmpty()) {
                topicRepository.deleteAllById(coursePath.getTopics());
                log.info("Deleted {} topics for course path {}", coursePath.getTopics().size(), coursePathId);
            }

            // Delete user progress records for this course path
            List<UserCourseProgressEntity> progressRecords = userCourseProgressRepository.findByCoursePathId(coursePathId);
            if (!progressRecords.isEmpty()) {
                userCourseProgressRepository.deleteAll(progressRecords);
                log.info("Deleted {} progress records for course path {}", progressRecords.size(), coursePathId);
            }

            // Remove course path from users' enrolled lists
            List<UserEntity> enrolledUsers = userRepository.findByEnrolledCoursePathsContaining(coursePathId);
            for (UserEntity user : enrolledUsers) {
                if (user.getEnrolledCoursePaths() != null) {
                    user.getEnrolledCoursePaths().remove(coursePathId);
                    userRepository.save(user);
                }
            }
            if (!enrolledUsers.isEmpty()) {
                log.info("Removed course path {} from {} users' enrolled lists", coursePathId, enrolledUsers.size());
            }

            // Finally delete the course path
            coursePathRepository.delete(coursePath);
            log.info("Successfully deleted course path with ID: {}", coursePathId);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting course path {}: {}", coursePathId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error deleting course path");
        }
    }

    public UserEntity searchUserByEmail(String email) {
        try {
            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }

            Optional<UserEntity> userOptional = userRepository.findByEmail(email.toLowerCase().trim());
            return userOptional.orElse(null);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error searching user by email {}: {}", email, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error searching user");
        }
    }

    public UserEntity grantAdminPrivilege(String userId) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (user.getRoles() == null) {
                user.setRoles(new ArrayList<>());
            }

            if (user.getRoles().contains("ADMIN")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has admin privileges");
            }

            user.getRoles().add("ADMIN");
            userRepository.save(user);
            
            log.info("Granted admin privileges to user with ID: {}", userId);
            return user;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error granting admin privilege to user {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error granting admin privilege");
        }
    }

    public UserEntity revokeAdminPrivilege(String userId) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (user.getRoles() == null || !user.getRoles().contains("ADMIN")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have admin privileges");
            }

            user.getRoles().remove("ADMIN");
            userRepository.save(user);
            
            log.info("Revoked admin privileges from user with ID: {}", userId);
            return user;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error revoking admin privilege from user {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error revoking admin privilege");
        }
    }
}
