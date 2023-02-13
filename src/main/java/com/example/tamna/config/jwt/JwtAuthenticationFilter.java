package com.example.tamna.config.jwt;

import com.example.tamna.model.Token;
import com.example.tamna.model.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class  JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Value("${AUTHORIZATION_HEADER}")
    private String AUTHORIZATION_HEADER;

    @Value("${REAUTHORIZATION_HEADER}")
    private String REAUTHORIZATION_HEADER;

    @Value("${ADMIN_HEADER}")
    private String ADMINAUTHORIZATION_HEADER;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 로그: 요청 url, method
        log.info("doFilter JWTFilter, uri: {}, method: {}", ((HttpServletRequest) request).getRequestURI(), request.getMethod());
        if(request.getMethod().equals("OPTIONS")){
            System.out.println("JWT 토큰 체크 x");
        }else if (request.getRequestURI().startsWith("/auth") || request.getRequestURI().startsWith("/admin")) {
            if (request.getRequestURI().startsWith("/auth") || request.getRequestURI().startsWith("/admin/login") || request.getRequestURI().startsWith("/admin/logout")) {
                System.out.println("로그인 및 로그아웃 시 토큰 검증 pass");
            } else {
                // admin 토큰 검증
                String adminAccessToken = jwtProvider.getHeaderToken(ADMINAUTHORIZATION_HEADER, request);
                Map<Boolean, String> accessResult = jwtProvider.validateToken(adminAccessToken); // 토큰검증
                if (!accessResult.isEmpty() && accessResult.containsKey(true) && accessResult.containsValue("success")) {
                    UserDto user = jwtProvider.checkUser(adminAccessToken);
                    if (!user.getRoles().equals("ADMIN")) {
                        System.out.println("어드민페이지 권한 없음");
                        response.sendError(404);
                    } else {
                        System.out.println("어드민토큰 유효");
                        response.setHeader(ADMINAUTHORIZATION_HEADER, tokenPrefix + adminAccessToken);
                    }
                } else {
                    // 헤더에 토큰이 실리지 않았을 때 및 토큰이 유효하지 않을 때
                    System.out.println("어드민 토큰 유효하지 않음");
                    response.sendError(403);
                }
            }
        } else {
            System.out.println("########토큰 검증 필터###########");

            // 헤더에서 access토큰 & refresh토큰 가져옴
            String accessToken = jwtProvider.getHeaderToken(AUTHORIZATION_HEADER, request);
            String refreshToken = jwtProvider.getHeaderToken(REAUTHORIZATION_HEADER, request);

            if(accessToken != null && refreshToken != null){
                Map<Boolean, String> accessResult = jwtProvider.validateToken(accessToken);  // accessToken 검증
                if (!accessResult.isEmpty() && accessResult.containsKey(true)) {
                    if (accessResult.containsValue("success")) { // accessToken 유효
                        response.setHeader(AUTHORIZATION_HEADER, tokenPrefix + accessToken);
                        response.setHeader(REAUTHORIZATION_HEADER, tokenPrefix + refreshToken);
                        System.out.println("accessToken 유효");
                    } else {
                        // accessToken이 만료되었을 때
                        Token checkRefresh = jwtProvider.checkRefresh(refreshToken); // refreshToken
                        System.out.println(checkRefresh);
                        if (checkRefresh != null) {
                            Map<Boolean, String> refreshResult = jwtProvider.validateToken(refreshToken); // refreshToken 검증
                            if (!refreshResult.isEmpty() && refreshResult.containsKey(true) && refreshResult.containsValue("success")) { // 유효
                                String newAccessToken = jwtProvider.createAccessToken(checkRefresh.getUserId());
                                System.out.println("access 재발급");
                                response.setHeader(AUTHORIZATION_HEADER, tokenPrefix + newAccessToken);
                                response.setHeader(REAUTHORIZATION_HEADER, tokenPrefix + refreshToken);
                            } else { // 유효x -> 로그아웃 처리
                                System.out.println("refresh만료, 로그아웃 처리");
                                jwtProvider.deleteToken(refreshToken);
                                response.sendError(403);
                            }
                        } else {
                            System.out.println("DB에 같은 refresh없음");
                            // refreshToken 오류
                            response.sendError(403);
                        }
                    }
                } else {
                    System.out.println("access 유효하지 않음");
                    // 헤더에 accessToken이 실리지 않았거나 유효하지 않을 경우
                    response.sendError(403);
                }
            }
        }
            filterChain.doFilter(request, response);
        }
}
