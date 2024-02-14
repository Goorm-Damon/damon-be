package damon.backend.jwt;

import damon.backend.dto.login.CustomOAuth2User;
import damon.backend.dto.login.OAuth2Response;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        log.info(authorization);
        System.out.println(authorization);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
        
            log.error("권한이 없습니다.");
            filterChain.doFilter(request, response);

            //헤더에 토큰 없이 페이지 요청이 온다면 접근 불가처리 (필수)
            return;
        }

        System.out.println("authorization now");
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {

            log.error("토큰이 만료되었습니다.");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰에서 정보 획득
        String providername = jwtUtil.getProvidername(token);
        String[] parts = providername.split(" ");
        String provider = parts[0];
        String providerId = parts[1];


        // OAuth2Response 인터페이스를 구현하는 익명 클래스 생성
        OAuth2Response oAuth2Response = new OAuth2Response() {
            @Override
            public String getProvider() {
                return provider;
            }

            @Override
            public String getProviderId() {
                return providerId;
            }

            @Override
            public String getName() {
                // 이름이 토큰에 포함되어 있지 않은 경우 처리
                return null;
            }

            @Override
            public String getEmail() {
                // 이메일이 토큰에 포함되어 있지 않은 경우 처리
                return null;
            }

            @Override
            public String getProfileImgUrl() {
                // 프로필 이미지 URL이 토큰에 포함되어 있지 않은 경우 처리
                return null;
            }
        };

        // Member 정보를 사용하여 CustomOAuth2User 객체 생성
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2Response);
        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null,  customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
