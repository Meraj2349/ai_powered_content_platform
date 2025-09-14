package org.example.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.model.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {

    // Find progress by user
    Optional<UserProgress> findByUserId(String userId);

    // Find progress by learning path
    @Query("{ 'learningPaths.learningPathId': ?0 }")
    List<UserProgress> findByLearningPathId(String learningPathId);

    // Find progress by course
    @Query("{ 'courses.courseId': ?0 }")
    List<UserProgress> findByCourseId(String courseId);

    // Find progress by user and learning path
    @Query("{ $and: [ { 'userId': ?0 }, { 'learningPaths.learningPathId': ?1 } ] }")
    List<UserProgress> findByUserIdAndLearningPathId(String userId, String learningPathId);

    // Find progress by user and course
    @Query("{ $and: [ { 'userId': ?0 }, { 'courses.courseId': ?1 } ] }")
    Optional<UserProgress> findByUserIdAndCourseId(String userId, String courseId);

    // Find progress by course status
    @Query("{ 'courses.status': ?0 }")
    List<UserProgress> findByCourseStatus(String status);

    // Find progress by user and course status
    @Query("{ $and: [ { 'userId': ?0 }, { 'courses.status': ?1 } ] }")
    List<UserProgress> findByUserIdAndCourseStatus(String userId, String status);

    // Find completed courses by user
    @Query(value = "{ $and: [ { 'userId': ?0 }, { 'courses.status': 'COMPLETED' } ] }", sort = "{ 'courses.completionDate': -1 }")
    List<UserProgress> findCompletedCoursesByUser(String userId);

    // Find progress with minimum completion percentage
    @Query("{ 'courses.completionPercentage': { $gte: ?0 } }")
    List<UserProgress> findByMinCompletionPercentage(double minPercentage);

    // Find recently accessed progress
    @Query("{ 'courses.lastActivityDate': { $gte: ?0 } }")
    List<UserProgress> findByLastAccessedAtAfter(LocalDateTime date);

    // Find progress by user with recent activity
    @Query("{ $and: [ { 'userId': ?0 }, { 'courses.lastActivityDate': { $gte: ?1 } } ] }")
    List<UserProgress> findRecentProgressByUser(String userId, LocalDateTime since);

    // Get user's learning statistics
    @Query("{ 'userId': ?0 }")
    List<UserProgress> findUserLearningStats(String userId);

    // Find users who completed a specific course
    @Query("{ $and: [ { 'courses.courseId': ?0 }, { 'courses.status': 'COMPLETED' } ] }")
    List<UserProgress> findUsersWhoCompletedCourse(String courseId);

    // Count completed courses by user
    @Query(value = "{ $and: [ { 'userId': ?0 }, { 'courses.status': 'COMPLETED' } ] }", count = true)
    long countCompletedCoursesByUser(String userId);

    // Find progress needing attention (started but not accessed recently)
    @Query("{ $and: [ " +
            "{ 'userId': ?0 }, " +
            "{ 'courses.status': 'IN_PROGRESS' }, " +
            "{ 'courses.lastActivityDate': { $lt: ?1 } } ] }")
    List<UserProgress> findStaleProgress(String userId, LocalDateTime cutoffDate);

    // Find top performing users for a course
    @Query(value = "{ 'courses.courseId': ?0 }", sort = "{ 'courses.completionPercentage': -1, 'courses.timeSpent': 1 }")
    List<UserProgress> findTopPerformersForCourse(String courseId);
}
