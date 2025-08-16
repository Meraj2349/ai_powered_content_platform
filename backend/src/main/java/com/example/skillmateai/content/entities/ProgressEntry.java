package com.example.skillmateai.content.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressEntry {
    
    private String topicId;
    private boolean isCovered;
    private long lastUpdated;
}
