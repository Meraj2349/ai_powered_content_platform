package com.example.skillmateai.content.repositories;

import com.example.skillmateai.content.entities.TopicEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TopicRepository extends MongoRepository<TopicEntity, String> {
    
    List<TopicEntity> findByTagsContaining(String tag);
    List<TopicEntity> findByPrerequisitesContaining(String prerequisiteId);
    List<TopicEntity> findByNameContainingIgnoreCase(String name);
}
