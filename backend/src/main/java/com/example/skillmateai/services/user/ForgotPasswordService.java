package com.example.skillmateai.services.user;


import com.example.skillmateai.entities.user.UserEntity;
import com.example.skillmateai.repositories.UserRepository;
import com.example.skillmateai.utilities.CreateResponseUtil;
import com.example.skillmateai.utilities.GetAuthenticatedUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class ForgotPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CreateResponseUtil createResponseUtil;
    @Autowired
    private UserService userService;

    @Transactional
    public ResponseEntity resetPasswordWithoutPreviousPassword(String newPassword, String email) throws Exception {
        try{
            
            if(email == null || email.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Email is required"));
            }
            
            if(newPassword == null || newPassword.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "New password is required"));
            }
            
            ResponseEntity<UserEntity> userResponse = userService.findUser(email, "email");

            if(userResponse.getStatusCode() != HttpStatus.OK){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseUtil.createResponseBody(false, "User with this email does not exist"));
            }

            if(newPassword.length() < 8 || newPassword.length() > 50){

                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Password must be between 8 and 50 characters"));

            }else{
                userResponse.getBody().setPassword((passwordEncoder.encode(newPassword)));
                userRepository.save(userResponse.getBody());
                return ResponseEntity.ok().body(createResponseUtil.createResponseBody(true, "Password successfully changed"));

            }

        }catch (Exception e){
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }


}
