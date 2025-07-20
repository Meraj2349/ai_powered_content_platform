package com.example.skillmateai.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordVerifyRequest {
    
    private String email;
    private String verificationCode;
    private String newPassword;
}
