package com.example.skillmateai.entities.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user-preference-data")
@Getter
@Setter
@AllArgsConstructor
public class UserPreferencesEntity {

    @Id
    String id;

    private String userEmail;
    private String preferredLanguage;
    private String preferredTheme;
    private String profilePictureUrl;
    private boolean pushNotificationsEnabled;
    private boolean emailNotificationsEnabled;
    private boolean subscribedToNewsletter;

}
