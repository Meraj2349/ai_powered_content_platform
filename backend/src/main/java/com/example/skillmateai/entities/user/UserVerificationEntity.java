package com.example.skillmateai.entities.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user-verification-data")
@Getter
@Setter
@AllArgsConstructor
public class UserVerificationEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userEmail;

    private String verificationCode;
    private Instant verificationCodeExpirationTime;

}
