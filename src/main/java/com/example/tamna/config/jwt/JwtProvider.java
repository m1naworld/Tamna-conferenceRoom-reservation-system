package com.example.tamna.config.jwt;

import com.example.tamna.mapper.TokenMapper;
import com.example.tamna.mapper.UserMapper;
import com.example.tamna.model.Token;
import com.example.tamna.model.UserDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.*;


@Service
@RequiredArgsConstructor
public class JwtProvider implements InitializingBean {

    private final TokenMapper tokenMapper;
    private final UserMapper userMapper;


    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accesstoken-validity-in-seconds}")
    private long accessTokenValidityInMilliSeconds;

    @Value("${jwt.refreshtoken-validity-in-seconds}")
    private long refreshTokenValidityInMilliSeconds;


    private Key key;


    // afterPropertiesSet() 빈 초기화 시 코드 구현
    @Override // Bean이 생성되고 주입받은 후 secretKey값을 Base64 Decode해서 Key변수에 할당
    public void afterPropertiesSet(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public java.sql.Date time() {
        final long miliseconds = System.currentTimeMillis();
        return new java.sql.Date(miliseconds);
    }


    public String createAccessToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);

        Date now = new Date();
        Date accessValidity = new Date(now.getTime() + accessTokenValidityInMilliSeconds * 1000);

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(accessValidity)
                .compact();

        return accessToken;
    }


    public String createRefreshToken(String userId){

        Date now = new Date();
        Date refreshValidity = new Date(now.getTime() + refreshTokenValidityInMilliSeconds * 1000);

        String refreshToken = Jwts.builder()
                .setSubject("")
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(refreshValidity)
                .compact();

        java.sql.Date today = time();
        Token result = tokenMapper.findToken(refreshToken);

        if(result == null) {
            int success = tokenMapper.insertToken(today, userId, refreshToken);
            return refreshToken;

        }else{ // db에 있는 refreshToken이 있는 경우 새로운 refreshToken 발급
            return createRefreshToken(userId);
        }


    }

    public Token checkRefresh(String refreshToken){
        return tokenMapper.findToken(refreshToken);
    }

    public String getUserIdFromJwt(String accessToken){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        return claims.getSubject();
    }

    public UserDto checkUser(String accessToken){
        String userId = getUserIdFromJwt(accessToken);
        return userMapper.findByUserId(userId);
    }

    public String getHeaderToken(String headerKey, HttpServletRequest request){
        String bearerAccessToken = request.getHeader(headerKey);

        if (StringUtils.hasText(bearerAccessToken) && bearerAccessToken.startsWith("Bearer ")){
            bearerAccessToken = bearerAccessToken.substring(7);
        }
        return bearerAccessToken;
    }

    public String getResHeaderAccessToken(String headerKey, HttpServletResponse response){
        String bearerAccessToken = response.getHeader(headerKey);

        if (StringUtils.hasText(bearerAccessToken) && bearerAccessToken.startsWith("Bearer ")){
            bearerAccessToken = bearerAccessToken.substring(7);
        }
        return bearerAccessToken;
    }


    public Map<Boolean, String> validateToken(String token){
        Map<Boolean, String> result = new HashMap<>();
        try{
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            result.put(!claims.getBody().getExpiration().before(new Date()), "success");
            return result;
        }catch (ExpiredJwtException e){
            result.put(true, "fail");
            return result;
        }catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("잘못된 JWT 서명");
        }catch (UnsupportedJwtException e){
            System.out.println("지원되지 않는 JWT");
        }catch (IllegalStateException e){
            System.out.println("JWT 토큰 잘못됨");
        }catch (IllegalArgumentException e){
            System.out.println("JWT 토큰 없음");
        }
        result.put(false, "유호하지 않음");
        return result;
    }


    public String deleteToken(String refreshToken) {
        int result = tokenMapper.deleteToken(refreshToken);
        if (result > 0) {
            return "success";
        }
        return "fail";
    }


}
