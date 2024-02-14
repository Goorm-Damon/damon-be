package damon.backend.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    // 엑세스 토큰 발급 관련 클래스 입니다

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        // HS256 알고리즘을 사용하여 비밀키를 생성
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 검증 1 = Providername
    public String getProvidername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("providername", String.class);
    }

    // 검증 2 = 토큰 만료시간
    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(String providername, Long expiredMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim("providername", providername)
                .issuedAt(now)
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

//    수정필요
//
//    JWT 유효성 검증
//    public boolean validateJwt(String token) {
//        try {
//            Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
//            return !claims.getPayload().getExpiration().before(new Date());
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    리프레시 토큰 생성
//    public String createRefreshToken(String providername, Long durationMs) {
//        Date now = new Date();
//        return Jwts.builder()
//                .setSubject(providername)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + durationMs))
//                .signWith(secretKey)
//                .compact();
//    }
}