package com.example.skillmateai.content.repositories;

import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserCourseProgressRepository extends MongoRepository<UserCourseProgressEntity, String> {
    
    List<UserCourseProgressEntity> findByUserId(String userId);
    Optional<UserCourseProgressEntity> findByUserIdAndCoursePathId(String userId, String coursePathId);
    List<UserCourseProgressEntity> findByCoursePathId(String coursePathId);
    List<UserCourseProgressEntity> findByReadinessGreaterThanEqual(Integer readiness);
}
