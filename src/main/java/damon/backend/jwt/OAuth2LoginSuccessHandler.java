package damon.backend.jwt;

import damon.backend.dto.login.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    //JWTUtil 주입
    private final JWTUtil jwtUtil;

    // 프론트엔드의 리디렉션 URL
    private final String frontendRedirectUrl = "http://localhost:3000/main";


    // 로그인 성공 시 Jwt 토큰 발급
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String providername = customOAuth2User.getProvidername();

        // 로그인 성공 로그 기록
        log.info("로그인이 성공했습니다. 사용자: " + customOAuth2User.getProvidername());

        // jwt 토큰 발급
        String token = jwtUtil.createJwt(providername, 3600000L);

        log.info("JWT 토큰 : " + token);

        response.addHeader("Authorization", "Bearer " + token);

        // 요청 헤더 로그
        log.info("요청 헤더로그: ");
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                log.info("Header Name: {}, Header Value: {}", headerName, request.getHeader(headerName)));

        // 응답 헤더 로그
        log.info("응답 헤더로그: ");
        response.getHeaderNames().forEach(headerName ->
                log.info("Header Name: {}, Header Value: {}", headerName, response.getHeader(headerName)));

        // 리디렉션 URL에 토큰 추가
        String redirectUrlWithToken = frontendRedirectUrl + "?token=" + token;

        // 클라이언트를 리디렉션 URL로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, redirectUrlWithToken);

    }



}

