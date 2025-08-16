package com.example.skillmateai.content.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "content_topic")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicEntity {
    
    @Id
    private String id;
    
    private String name;
    private String description;
    private VideoInfo videoInfo;
    private List<String> prerequisites; // could be list of topic IDs
    private Integer estimatedTimeMin;
    private List<String> tags;
}
