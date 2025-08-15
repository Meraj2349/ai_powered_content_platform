package com.example.skillmateai.user.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private long verificationCodeExpirationTime;

}
