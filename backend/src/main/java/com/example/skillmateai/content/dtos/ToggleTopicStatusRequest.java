package com.example.skillmateai.content.dtos;

import lombok.Data;

@Data
public class ToggleTopicStatusRequest {
    private String progressId;
    private String topicId;
}
