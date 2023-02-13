package com.example.tamna.controller;

import com.example.tamna.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${AUTHORIZATION_HEADER}")
    private String AUTHORIZATION_HEADER;

    @Value("${REAUTHORIZATION_HEADER}")
    private String REAUTHORIZATION_HEADER;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private final AuthService authService;

    @Data
    static class UserId{
        private String userId;
    }

    @ApiOperation(value = "JWT 발급")
    @PostMapping(value = "/login")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createToken(@RequestBody UserId userId, HttpServletResponse response){
        String getUserId = userId.userId;
        Map<String, String> tokenMap =  authService.login(getUserId);

        if(tokenMap.get("message") != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(tokenMap);
        }else{
            response.addHeader(AUTHORIZATION_HEADER, tokenPrefix + tokenMap.get("access"));
            response.addHeader(REAUTHORIZATION_HEADER, tokenPrefix + tokenMap.get("refresh"));
            Map<String, String> map = new HashMap<>();
            map.put("message", "success");
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
    }



    @ApiOperation(value = "로그아웃")
    @GetMapping(value = "/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request){
        Map<String, String> map = new HashMap<>();
        String result = authService.logOutCheckUser(request);
        map.put("message", result);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


}
