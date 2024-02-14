package damon.backend.config;

import damon.backend.jwt.JWTFilter;
import damon.backend.jwt.JWTUtil;
import damon.backend.jwt.OAuth2LoginFailureHandler;
import damon.backend.jwt.OAuth2LoginSuccessHandler;
import damon.backend.oauth2.CustomClientRegistrationRepo;
import damon.backend.service.CustomOauth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 데이터 담을 유저 서비스
    private final CustomOauth2UserService customOauth2UserService;

    // 로그인 성공 시 토큰 발급
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    // 로그인 실패
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    // JWTUtil / 필터 주입
    private final JWTUtil jwtUtil;
    private final JWTFilter jwtFilter;

    private final CustomClientRegistrationRepo customClientRegistrationRepo;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //cors 설정
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                })));

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //JWTFilter 등록
        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        //oauth2 방식 설정 (client가 아닌 login으로 설정해야 자동으로 필터 구현된다)
        http
                .oauth2Login((oauth2) -> oauth2
                        .loginPage("/login")
                        .clientRegistrationRepository(customClientRegistrationRepo.clientRegistrationRepository())
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOauth2UserService)) // 사용자 정보 가져오기
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 핸들러 추가
                        .failureHandler(oAuth2LoginFailureHandler));

        //특정 경로로 이동할 때 인가 작업 처리
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/oauth2/**", "/login/**").permitAll() // 로그인 창은 모두 접근 가능
                        .anyRequest().authenticated()); // 나머지 경로는 로그인 한 사람만 접근가능

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        return http.build();
    }
}