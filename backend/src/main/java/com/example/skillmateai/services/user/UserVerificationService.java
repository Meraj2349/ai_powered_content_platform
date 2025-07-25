package com.example.skillmateai.services.user;

import com.example.skillmateai.entities.user.UserEntity;
import com.example.skillmateai.entities.user.UserVerificationEntity;
import com.example.skillmateai.repositories.UserRepository;
import com.example.skillmateai.repositories.UserVerificationRepository;
import com.example.skillmateai.utilities.CreateResponseUtil;
import com.example.skillmateai.utilities.GenerateAndValidateStringUtil;
import com.example.skillmateai.utilities.GetAuthenticatedUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


@Service
@Slf4j
public class UserVerificationService {

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired
    private VerificationEmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreateResponseUtil createResponseUtil;

    public UserVerificationEntity createUserVerificationEntity(String userEmail){
        UserVerificationEntity userVerificationEntity =
                new UserVerificationEntity(GenerateAndValidateStringUtil.generateUniqueString(), userEmail, "", Instant.now());

        return userVerificationRepository.save(userVerificationEntity);
    }

    public ResponseEntity sendVerificationCodeEmail(UserEntity userEntity, String emailSubject, String emailBody, String responseMessage) throws Exception {
        try{

            UserVerificationEntity userVerificationEntity ;
            Optional<UserVerificationEntity> optionalUserVerificationEntity = userVerificationRepository.findByUserEmail(userEntity.getEmail());
            if(!optionalUserVerificationEntity.isPresent()){
                UserVerificationEntity newUserVerificationEntity = createUserVerificationEntity(userEntity.getEmail());
//                userEntity.setUserVerificationEntityId(newUserVerificationEntity.getId());
                userRepository.save(userEntity);
                userVerificationEntity = newUserVerificationEntity;

            }else{
                userVerificationEntity = optionalUserVerificationEntity.get();
            }

            String verificationCode = GenerateAndValidateStringUtil.generateOtp(6);
            userVerificationEntity.setVerificationCode(verificationCode);
            userVerificationEntity.setVerificationCodeExpirationTime(Instant.now().plus(Duration.ofMinutes(5)));
            userVerificationRepository.save(userVerificationEntity);
            emailService.sendEmail(userEntity.getEmail(), emailSubject, verificationCode, emailBody);
            log.debug("Email sent");
            return ResponseEntity.ok().body(createResponseUtil.createResponseBody(true, responseMessage));

        }catch (Exception e){
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    public ResponseEntity verifyVerificationCode(UserEntity userEntity, String verificationCode, Boolean isForgotPasswordVerification, String responseMessage) throws Exception {
        try{
            Optional<UserVerificationEntity> userVerificationEntity = userVerificationRepository.findByUserEmail(userEntity.getEmail());

            if(userVerificationEntity.isPresent()){
                if(userVerificationEntity.get().getVerificationCode().equals("")){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createResponseUtil.createResponseBody(false, "This user has not requested for verification code"));

                }else if(!userVerificationEntity.get().getVerificationCode().equals(verificationCode)){

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createResponseUtil.createResponseBody(false, "This code is incorrect"));

                }else if(userVerificationEntity.get().getVerificationCode().equals(verificationCode)
                        && userVerificationEntity.get().getVerificationCodeExpirationTime().isBefore(Instant.now())){

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createResponseUtil.createResponseBody(false, "This code has expired"));

                }else{

                    userVerificationEntity.get().setVerificationCode("");
                    userVerificationRepository.save(userVerificationEntity.get());

                    if (!isForgotPasswordVerification) {
                        userEntity.setVerified(true);
                        userRepository.save(userEntity);
                        return ResponseEntity.ok().body(createResponseUtil.createResponseBody(true, responseMessage));
                    }

                    return  ResponseEntity.ok().build();

                }
            }else{
                throw  new Exception();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
