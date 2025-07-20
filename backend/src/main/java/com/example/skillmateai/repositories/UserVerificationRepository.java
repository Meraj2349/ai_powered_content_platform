package com.example.skillmateai.repositories;


import com.example.skillmateai.entities.user.UserVerificationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserVerificationRepository extends MongoRepository<UserVerificationEntity, String> {

    Optional<UserVerificationEntity> findByUserEmail(String userEmail);

    Boolean deleteByUserEmail(String userEmail);

}
