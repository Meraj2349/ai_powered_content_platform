package com.example.skillmateai.user.utilities;

import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class GetAuthenticatedUserUtil {

    @Autowired
    private UserRepository userRepository;

    public UserEntity getAuthenticatedUser() {
       try{
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           
           if(authentication == null || !authentication.isAuthenticated()){
               log.warn("No authentication found in security context");
               return null;
           }
           
           String email = authentication.getName();
           
           if(email == null || email.isEmpty()){
               log.warn("No email found in authentication principal");
               return null;
           }
           
           Optional<UserEntity> user = userRepository.findByEmail(email);
           if(user.isPresent()) {
               return user.get();
           }else{
               log.warn("User not found for authenticated email: {}", email);
               return null;
           }
       }catch(Exception e){
           log.error("Error getting authenticated user: {}", e.getMessage());
           return null;
       }
    }
}