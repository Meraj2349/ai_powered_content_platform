package com.example.skillmateai.utilities;

import com.example.skillmateai.entities.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Autowired
    CreateResponseUtil createResponseUtil;

    @Value("${app.jwtKey}")
    private String jwtSecretKey;

    @Value("${app.refreshTokenKey}")
    private String refreshTokenSecretKey;



    private SecretKey getSigningKey(boolean isRefresh) {
        return isRefresh ? Keys.hmacShaKeyFor(refreshTokenSecretKey.getBytes()) : Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
    }

    public String extractEmail(String token, boolean isRefresh) {
        Claims claims = extractAllClaims(token, isRefresh);
        return claims.getSubject();
    }

    public Date extractExpiration(String token, boolean isRefresh) {
        return extractAllClaims(token, isRefresh).getExpiration();
    }

    private Claims extractAllClaims(String token, boolean isRefresh) {
        return Jwts.parser()
                .verifyWith(getSigningKey(isRefresh))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token, boolean isRefresh) {
        return (Boolean) extractExpiration(token, isRefresh).before(new Date());
    }

    public String generateToken(String email, boolean isRefresh) throws Exception {
        if(email == null || email.isEmpty()){
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, isRefresh);
    }

    private String createToken(Map<String, Object> claims, String subject, boolean isRefresh) throws Exception {
       try{
           long tokenLifetimeInMillis ;
           if(isRefresh){
               tokenLifetimeInMillis = 1000L * 60 * 60 * 24 * 30; // 30 days
           }else{
               tokenLifetimeInMillis = 1000L * 60 * 60; // 1 hour
           }

           return Jwts.builder()
                   .claims(claims)
                   .subject(subject)
                   .header().empty().add("typ", "JWT")
                   .and()
                   .issuedAt(new Date(System.currentTimeMillis()))
                   .expiration(new Date(System.currentTimeMillis() + tokenLifetimeInMillis))
                   .signWith(getSigningKey(isRefresh))
                   .compact();
       }catch(Exception e){
           log.error(e.getMessage());
           throw new Exception(e.getMessage());
       }
    }


    public Boolean validateToken(String token, boolean isRefresh) {
        if(token == null || token.isEmpty()){
            log.warn("Token validation failed: token is null or empty");
            return false;
        }
        
        try {
            extractAllClaims(token, isRefresh); //this will throw error if jwt is not signed with the correct signing key
            return (Boolean) !isTokenExpired(token, isRefresh);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return (Boolean) false;
        }
    }

    public ResponseEntity<Map> generateTokenAndUserInfoResponse(UserEntity user, String message) throws Exception {
        try{
            
            if(user == null){
                throw new IllegalArgumentException("User cannot be null");
            }
            
            if(message == null || message.isEmpty()){
                message = "Operation successful";
            }
            
            String jwt = generateToken(user.getEmail(), false);
            String refreshToken = generateToken(user.getEmail(), true);

            if(jwt != null && refreshToken != null) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(createResponseUtil
                                .createResponseBody(true, message, "userInfo", createResponseUtil.createUserInfoMap(user), "jwt", jwt, "refreshToken", refreshToken));
            }else{
                return ResponseEntity.internalServerError()
                        .body(createResponseUtil.createResponseBody(false, "An error occurred while generating response"));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e);
        }
    }


}
