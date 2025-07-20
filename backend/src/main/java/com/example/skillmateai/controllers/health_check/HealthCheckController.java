package com.example.skillmateai.controllers.health_check;


import com.example.skillmateai.utilities.CreateResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/public")
public class HealthCheckController {

    @Autowired
    private CreateResponseUtil createResponseUtil;

    @GetMapping("health-check")
    public ResponseEntity healthCheck() {
       try{
           return ResponseEntity.ok().body(createResponseUtil.createResponseBody(true, "API is up and running"));
       }catch(Exception e){
           log.error(e.getMessage());
           return ResponseEntity.internalServerError()
                   .body(createResponseUtil.createResponseBody(false, "An error occurred"));
       }
    }
}
