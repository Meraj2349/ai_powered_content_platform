package com.example.skillmateai.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNameRequest {
    
    private String newFirstName;
    private String newLastName;
}
