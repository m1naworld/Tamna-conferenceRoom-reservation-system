package com.example.tamna.config.jwt;

import com.example.tamna.model.Token;
import com.example.tamna.model.UserDto;

import lombok.RequiredArgsConstructor;

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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		if (!request.getMethod().equals("OPTIONS") && (request.getRequestURI().startsWith("/auth")
			|| request.getRequestURI().startsWith("/admin"))) {
			if (!request.getRequestURI().startsWith("/auth") && !request.getRequestURI().equals("/admin/login")
				&& !request.getRequestURI().startsWith("/admin/logout")) {

				// admin 토큰 검증
				String adminAccessToken = jwtProvider.getHeaderToken(ADMINAUTHORIZATION_HEADER, request);
				Map<Boolean, String> accessResult = jwtProvider.validateToken(adminAccessToken);

				if (accessResult.isEmpty() && !accessResult.containsKey(true)) {
					response.sendError(403);
					return;
				}

				UserDto user = jwtProvider.checkUser(adminAccessToken);
				if (!user.getRoles().equals("ADMIN")) {
					response.sendError(404); // 어드민 페이지 권한 x
					return;
				}
				response.setHeader(ADMINAUTHORIZATION_HEADER, tokenPrefix + adminAccessToken);
			}
		} else if (!request.getMethod().equals("OPTIONS")) {

			String accessToken = jwtProvider.getHeaderToken(AUTHORIZATION_HEADER, request);
			String refreshToken = jwtProvider.getHeaderToken(REAUTHORIZATION_HEADER, request);

			if (accessToken == null) {
				response.sendError(403);
				return;
			}

			Map<Boolean, String> accessResult = jwtProvider.validateToken(accessToken);
			if (accessResult.isEmpty() && !accessResult.containsKey(true)) {
				response.sendError(403);
				return;
			}
			if (accessResult.containsValue("success")) {
				response.setHeader(AUTHORIZATION_HEADER, tokenPrefix + accessToken);
				response.setHeader(REAUTHORIZATION_HEADER, tokenPrefix + refreshToken);

			} else { // assess 만료

				if (refreshToken == null) {
					response.sendError(403);
					return;
				}

				Token checkRefresh = jwtProvider.checkRefresh(refreshToken);
				if (checkRefresh == null) {
					response.sendError(403);
					return;
				}

				Map<Boolean, String> refreshResult = jwtProvider.validateToken(refreshToken);
				if (refreshResult.isEmpty() && !refreshResult.containsKey(true)
					&& !refreshResult.containsValue("success")) {
					jwtProvider.deleteToken(refreshToken);
					response.sendError(403);
					return;
				}
				String newAccessToken = jwtProvider.createAccessToken(checkRefresh.getUserId());
				response.setHeader(AUTHORIZATION_HEADER, tokenPrefix + newAccessToken);
				response.setHeader(REAUTHORIZATION_HEADER, tokenPrefix + refreshToken);
			}
		}
		filterChain.doFilter(request, response);
	}
}
