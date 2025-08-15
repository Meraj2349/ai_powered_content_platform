package com.example.skillmateai.content.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "content_coursePath")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePathEntity {
    
    @Id
    private String id;

    private String creatorId;
    private String title;
    private String description;
    private String targetLevel;// e.g. Beginner, Intermediate, Advanced
    private long createdAt;
    private String createdBy;
    private List<String> topics; // references to topic IDs
    private List<ReviewEntity> reviews; // Course ratings and reviews
    private Double averageRating; // Calculated average rating
}
