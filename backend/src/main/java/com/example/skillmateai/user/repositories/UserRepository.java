package com.example.skillmateai.user.repositories;


import com.example.skillmateai.user.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByEnrolledCoursePathsContaining(String coursePathId);




}