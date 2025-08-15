package com.example.skillmateai.content.dtos;

import lombok.Data;

@Data
public class AddReviewRequest {
    private String coursePathId;
    private Integer rating; // Rating out of 5
    private String comment;
}
