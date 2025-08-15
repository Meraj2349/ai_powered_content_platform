package com.example.skillmateai.content.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoInfo {
    
    private String youtubeUrl;
    private String title;
    private Integer startTime;
    private Integer endTime;
}
