package com.example.skillmateai.content.repositories;

import com.example.skillmateai.content.entities.CoursePathEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CoursePathRepository extends MongoRepository<CoursePathEntity, String> {
    
    List<CoursePathEntity> findByTargetLevel(String targetLevel);
    List<CoursePathEntity> findByCreatedBy(String createdBy);
    List<CoursePathEntity> findByCreatorId(String creatorId);
    List<CoursePathEntity> findByTitleContainingIgnoreCase(String title);
    List<CoursePathEntity> findByTopicsContaining(String topicId);
    
    // Fuzzy search methods for similar titles
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<CoursePathEntity> findByTitleRegex(String titlePattern);
}
