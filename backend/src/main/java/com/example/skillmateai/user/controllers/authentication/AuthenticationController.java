package com.example.skillmateai.user.controllers.authentication;

import com.example.skillmateai.user.dtos.LoginRequest;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.services.UserService;
import com.example.skillmateai.user.utilities.CreateResponseUtil;
import com.example.skillmateai.user.utilities.JwtUtil;
import com.example.skillmateai.user.utilities.MatchTextPatternUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    AuthenticationManager authenticationManager ;

    @Autowired
    private CreateResponseUtil createResponseUtil;

    @Autowired
    private MatchTextPatternUtil matchTextPatternUtil;


    @Transactional
    @PostMapping("signup")
    public ResponseEntity<Map<String,Object>> signup (@RequestBody UserEntity userEntity)  {
        try{
            if(userEntity == null){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Request body is required"));
            }

            if(userEntity.getEmail() == null || userEntity.getPassword() == null || userEntity.getFirstName() == null ||
               userEntity.getEmail().isEmpty() || userEntity.getPassword().isEmpty() || userEntity.getFirstName().isEmpty()){

                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Email, password or first name is empty"));

            }else if(!matchTextPatternUtil.isValidEmail(userEntity.getEmail())){

                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "This email address is not valid. You must use a JUST edu email"));

            }else if(userEntity.getPassword().length() < 8 || userEntity.getPassword().length() > 50){

                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Password must be between 8 and 50 characters"));

            }else{

                ResponseEntity<UserEntity> userResponse = userService.findUser(userEntity.getEmail(), "email");

                if(userResponse.getStatusCode().equals(HttpStatus.OK)){

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createResponseUtil.createResponseBody(false, "Another user with this email already exists"));

                }

                UserEntity createdUser = userService.createUser(userEntity, false);

                if(createdUser != null) {

                    ResponseEntity<Map<String,Object>> tokenResponse = jwtUtil.generateTokenAndUserInfoResponse(createdUser, "User successfully created");
                    return tokenResponse;

                }else {

                    return ResponseEntity.internalServerError()
                            .body(createResponseUtil.createResponseBody(false, "User creation failed, please try again"));

                }
            }

        }catch (Exception e) {

            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while creating new user"));

        }
    }


    @PostMapping("login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody LoginRequest loginRequest) {
        try{
            if(loginRequest == null){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Request body is required"));
            }

            System.out.println("login attempt");

            if(loginRequest.getEmail() == null || loginRequest.getPassword() == null ||
               loginRequest.getEmail().isEmpty() || loginRequest.getPassword().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Email or password is empty"));

            }else if(!matchTextPatternUtil.isValidEmail(loginRequest.getEmail())){

                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "This email address is not valid"));

            }else{
                try{
                    // First check if user exists BEFORE authenticating
                    ResponseEntity<UserEntity> userLookup = userService.findUser(loginRequest.getEmail(), "email");
                    if(!userLookup.getStatusCode().is2xxSuccessful() || userLookup.getBody() == null){
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(createResponseUtil.createResponseBody(false, "Account does not exist"));
                    }

                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                    );

                    ResponseEntity<Map<String,Object>> tokenResponse = jwtUtil.generateTokenAndUserInfoResponse(userLookup.getBody(), "Login successful");
                    return tokenResponse;

                }catch (org.springframework.security.authentication.BadCredentialsException e) {
                    log.error(e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(createResponseUtil.createResponseBody(false, "Email or password is incorrect"));
                }catch (Exception e) {
                    log.error(e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(createResponseUtil.createResponseBody(false, "Email or password is incorrect"));
                }
            }

        } catch (Exception e) {

            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while logging user in"));

        }
    }



}

