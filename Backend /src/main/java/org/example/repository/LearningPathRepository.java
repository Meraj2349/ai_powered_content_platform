package org.example.repository;

import org.example.model.LearningPath;
import org.example.model.PathStatus;
import org.example.model.PathType;
import org.example.model.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningPathRepository extends MongoRepository<LearningPath, String> {

    // Find learning paths by user
    List<LearningPath> findByUserId(String userId);

    // Find learning paths by status
    List<LearningPath> findByStatus(PathStatus status);

    // Find learning paths by type
    List<LearningPath> findByPathType(PathType pathType);

    // Find learning paths by target level
    List<LearningPath> findByTargetLevel(DifficultyLevel targetLevel);

    // Find learning paths by skill goal
    @Query("{ 'skillGoal': { $regex: ?0, $options: 'i' } }")
    List<LearningPath> findBySkillGoalContaining(String skillGoal);

    // Find active learning paths for user
    List<LearningPath> findByUserIdAndStatus(String userId, PathStatus status);

    // Find learning paths by preferred language
    List<LearningPath> findByPreferredLanguage(String language);

    // Find learning paths within duration range
    List<LearningPath> findByEstimatedDurationHoursBetween(int minHours, int maxHours);

    // Find recently created learning paths
    List<LearningPath> findByCreatedAtAfter(LocalDateTime date);

    // Find learning paths by preferred platforms
    @Query("{ 'preferredPlatforms': { $in: ?0 } }")
    List<LearningPath> findByPreferredPlatformsIn(List<String> platforms);

    // Find completed learning paths for user
    @Query("{ $and: [ { 'userId': ?0 }, { 'status': 'COMPLETED' } ] }")
    List<LearningPath> findCompletedPathsByUser(String userId);

    // Find learning paths with high completion rate
    @Query("{ 'progress.completionPercentage': { $gte: ?0 } }")
    List<LearningPath> findPathsWithHighCompletion(double minCompletionRate);

    // Count learning paths by user
    long countByUserId(String userId);

    // Find learning paths for recommendations
    @Query("{ $and: [ " +
            "{ 'skillGoal': { $regex: ?0, $options: 'i' } }, " +
            "{ 'targetLevel': ?1 }, " +
            "{ 'status': 'ACTIVE' } ] }")
    List<LearningPath> findSimilarPaths(String skillGoal, DifficultyLevel targetLevel);

    // Find popular learning paths
    @Query(value = "{}", sort = "{ 'createdAt': -1 }")
    Page<LearningPath> findPopularPaths(Pageable pageable);
}
