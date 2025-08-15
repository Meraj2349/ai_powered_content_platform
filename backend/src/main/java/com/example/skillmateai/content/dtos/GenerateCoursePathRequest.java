package com.example.skillmateai.content.dtos;

import lombok.Data;

@Data
public class GenerateCoursePathRequest {
    private String subject;
    private String difficulty; // beginner, intermediate, advanced
}
