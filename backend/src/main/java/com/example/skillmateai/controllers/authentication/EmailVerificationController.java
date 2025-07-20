package com.example.skillmateai.controllers.authentication;


import com.example.skillmateai.dtos.VerificationCodeRequest;
import com.example.skillmateai.entities.user.UserEntity;
import com.example.skillmateai.services.user.UserVerificationService;
import com.example.skillmateai.utilities.CreateResponseUtil;
import com.example.skillmateai.utilities.GetAuthenticatedUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/email-verification")
public class EmailVerificationController {

    @Autowired
    private GetAuthenticatedUserUtil getAuthenticatedUserUtil;

    @Autowired
    private UserVerificationService userVerificationService;

    @Autowired
    private CreateResponseUtil createResponseUtil;

    @Transactional
    @PostMapping("code")
    public ResponseEntity<Object> sendEmailVerificationCode() {
        try{
            UserEntity authenticatedUser = getAuthenticatedUserUtil.getAuthenticatedUser();
            System.out.println("Mail sent to: " + authenticatedUser.getEmail());

            if(authenticatedUser.isVerified()){

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createResponseUtil.createResponseBody(false, "This user is already verified"));
            }
            return userVerificationService.sendVerificationCodeEmail(authenticatedUser,
                    "Your email verification code",
                    "Use this code to verify your email in SkillMateAI",
                    "Email verification code sent");


        }catch(Exception e){

            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while sending verification code"));
        }
    }

    @Transactional
    @PostMapping("verify")
    public ResponseEntity<Object> verifyEmailVerificationCode(@RequestBody VerificationCodeRequest request) {

        try{
            
            if(request == null){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Request body is required"));
            }
            
            if(request.getVerificationCode() == null || request.getVerificationCode().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Verification code is required"));
            }
            
            String verificationCode = request.getVerificationCode();
            UserEntity authenticatedUser = getAuthenticatedUserUtil.getAuthenticatedUser();

            return userVerificationService.verifyVerificationCode(authenticatedUser, verificationCode, false, "Email verification code verified. User is now verified");


        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while verifying verification code"));

        }
    }
}
