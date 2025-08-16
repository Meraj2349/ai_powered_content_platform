package com.example.skillmateai.user.repositories;


import com.example.skillmateai.user.entities.UserVerificationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserVerificationRepository extends MongoRepository<UserVerificationEntity, String> {

    Optional<UserVerificationEntity> findByUserEmail(String userEmail);

    Boolean deleteByUserEmail(String userEmail);

}
