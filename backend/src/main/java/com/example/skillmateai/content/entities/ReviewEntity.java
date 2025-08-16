package com.example.skillmateai.content.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEntity {
    
    private String reviewerId; // User ID who gave the review
    private String reviewerName; // User name for display
    private Integer rating; // 1-5 stars
    private String comment;
    private long reviewDate;
}
