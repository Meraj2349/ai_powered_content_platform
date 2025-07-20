package com.example.skillmateai.controllers.authentication;

import com.example.skillmateai.dtos.EmailRequest;
import com.example.skillmateai.dtos.ForgotPasswordVerifyRequest;
import com.example.skillmateai.entities.user.UserEntity;
import com.example.skillmateai.services.user.ForgotPasswordService;
import com.example.skillmateai.services.user.UserService;
import com.example.skillmateai.services.user.UserVerificationService;
import com.example.skillmateai.utilities.CreateResponseUtil;
import com.example.skillmateai.utilities.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/forget-password")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CreateResponseUtil createResponseUtil;

    @Autowired
    UserVerificationService userVerificationService;

    @Transactional
    @PostMapping("code")
    public ResponseEntity<Object> sendForgotPasswordCodeUsingEmail(@RequestBody EmailRequest request) {
        try{
            
            if(request == null){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Request body is required"));
            }
            
            if(request.getEmail() == null || request.getEmail().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Email is required"));
            }
            
            String email = request.getEmail();
            log.info("email: " + email);
            ResponseEntity<UserEntity> response = userService.findUser(email, "email");
            if(response.getStatusCode() == HttpStatus.OK){
                return userVerificationService.sendVerificationCodeEmail(response.getBody(),
                        "Your forgot password verification code",
                        "Use this code to continue with resetting your password in SkillMateAI",
                        "Forgot password verification code sent");
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseUtil.createResponseBody(false, "User does not exist"));
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while sending password reset verification code"));
        }

    }


    @Transactional
    @PostMapping("verify-and-reset")
    public ResponseEntity<Map> verifyForgotPasswordVerificationCode(@RequestBody ForgotPasswordVerifyRequest request) {

        try{
            
            if(request == null){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Request body is required"));
            }
            
            if(request.getEmail() == null || request.getEmail().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Email is required"));
            }
            
            if(request.getVerificationCode() == null || request.getVerificationCode().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Verification code is required"));
            }
            
            if(request.getNewPassword() == null || request.getNewPassword().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "New password is required"));
            }
            
            String email = request.getEmail();
            String verificationCode = request.getVerificationCode();
            String newPassword = request.getNewPassword();

            ResponseEntity<UserEntity> userResponse = userService.findUser(email, "email");

            if(userResponse.getStatusCode() == HttpStatus.OK ){
                ResponseEntity verificationResponse = userVerificationService
                        .verifyVerificationCode(userResponse.getBody(), verificationCode, true, "");

                if(verificationResponse.getStatusCode() == HttpStatus.OK ){
                    ResponseEntity resetPasswordResponse = forgotPasswordService.resetPasswordWithoutPreviousPassword(newPassword, email);

                    if(resetPasswordResponse.getStatusCode() == HttpStatus.OK ){
                        return  jwtUtil.generateTokenAndUserInfoResponse(userResponse.getBody(), "Forgot password code was verified and password reset is successful");

                    }else{
                        return resetPasswordResponse;
                    }
                }else{
                    return verificationResponse;
                }

            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseUtil.createResponseBody(false, "User does not exist"));
            }

        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while verifying password reset verification code"));
        }
    }

}
