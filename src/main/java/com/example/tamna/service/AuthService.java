package com.example.tamna.service;

import com.example.tamna.config.jwt.JwtProvider;
import com.example.tamna.mapper.UserMapper;
import com.example.tamna.model.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

    @Value("${AUTHORIZATION_HEADER}")
    private String AUTHORIZATION_HEADER;

    @Value("${REAUTHORIZATION_HEADER}")
    private String REAUTHORIZATION_HEADER;

    // 로그인 시 토큰 생성
    public Map<String, String> login(String userId) {
        Map<String, String> map = new HashMap<>();
        UserDto user = userMapper.findByUserId(userId);

        if (user != null) {
            String access = jwtProvider.createAccessToken(user.getUserId());
            String refresh = jwtProvider.createRefreshToken(user.getUserId());

            map.put("access", access);
            map.put("refresh", refresh);
        } else {
            map.put("message", "fail");
        }
        return map;
    }

    public UserDto checkUser(HttpServletResponse response){
        String accessToken = jwtProvider.getResHeaderAccessToken(AUTHORIZATION_HEADER, response);
        if(accessToken!= null) {
           return jwtProvider.checkUser(accessToken);
        }
        return null;
    }

    public String logOutCheckUser(HttpServletRequest request){
        String accessToken = jwtProvider.getHeaderToken(AUTHORIZATION_HEADER, request);
        String refreshToken = jwtProvider.getHeaderToken(REAUTHORIZATION_HEADER, request);
        if(accessToken != null && refreshToken != null) {
            return jwtProvider.deleteToken(refreshToken);
        }else{
            return null;
        }
    }

}
