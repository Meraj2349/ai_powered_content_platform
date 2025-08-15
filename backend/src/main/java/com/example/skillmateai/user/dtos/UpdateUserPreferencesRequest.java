package com.example.skillmateai.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPreferencesRequest {
    
    private String preferredLanguage; // Optional: "ENGLISH" or "BANGLA"
    private Boolean isDarkTheme; // Optional: true for dark, false for light
    private Boolean pushNotificationsEnabled; // Optional
    private Boolean emailNotificationsEnabled; // Optional
    private Boolean subscribedToNewsletter; // Optional
}