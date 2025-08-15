package com.example.skillmateai.content.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "content_userCourseProgress")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCourseProgressEntity {
    
    @Id
    private String id;
    
    private String userId;
    private String coursePathId;
    private long startedAt;
    private Integer readiness; // 0-100
    private List<ProgressEntry> progress;
}
