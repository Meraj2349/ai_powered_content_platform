package com.example.skillmateai.controllers.authentication;


import com.example.skillmateai.utilities.CreateResponseUtil;
import com.example.skillmateai.utilities.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationTokenController {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private CreateResponseUtil createResponseUtil;

    @GetMapping("refresh")
    public ResponseEntity<Map> refreshToken(@RequestParam String refreshToken) {

        try{
            
            if(refreshToken == null || refreshToken.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Refresh token is required"));
            }

            if(jwtUtil.validateToken(refreshToken, true)){
                String email = jwtUtil.extractEmail(refreshToken, true);
                String newJwt = jwtUtil.generateToken(email, false);

                return ResponseEntity.ok(createResponseUtil.createResponseBody(true, "New JWT generated", "jwt", newJwt));

            }else{

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createResponseUtil.createResponseBody(false, "Refresh token is invalid or expired"));
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while refreshing JWT"));
        }
    }

    @GetMapping("token-verification")
    public ResponseEntity<Map> verifyToken(@RequestParam String token) {

        try{
            
            if(token == null || token.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(createResponseUtil.createResponseBody(false, "Token is required"));
            }

            if(jwtUtil.validateToken(token, false)){

                return ResponseEntity.ok(createResponseUtil.createResponseBody(true, "This a valid JWT"));

            }else if(jwtUtil.validateToken(token, true)){

                return ResponseEntity.ok(createResponseUtil.createResponseBody(true, "This a valid Refresh Token"));

            }else{

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createResponseUtil.createResponseBody(false, "This token is invalid or expired"));
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createResponseUtil.createResponseBody(false, "An error occurred while verifying the token"));
        }
    }
}
