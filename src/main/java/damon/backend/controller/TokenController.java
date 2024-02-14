package damon.backend.controller;//package home.oauth2.login;
//package home.oauth2.login.controller;
//
//import home.oauth2.login.jwt.JWTUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class TokenController {
//
//    // jwt 관리
//    @Autowired
//    private JWTUtil jwtUtil;
//
//    @PostMapping("/auth/token/refresh")
//    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String oldToken) {
//        // JWT 토큰의 'Bearer ' 접두어 제거
//        if (oldToken != null && oldToken.startsWith("Bearer ")) {
//            oldToken = oldToken.substring(7);
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token Format");
//        }
//
//        // JWT 토큰의 유효성 검증
//        if (!jwtUtil.validateJwt(oldToken)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or Expired Token");
//        }
//
//        // JWT 토큰에서 providername 추출
//        String providername = jwtUtil.getProvidername(oldToken);
//
//        // 새로운 JWT 토큰 생성
//        String newJwtToken = jwtUtil.createJwt(providername, 3600000L); // 1시간 유효한 새로운 JWT 토큰 생성
//
//        // 새로운 JWT 토큰을 응답으로 반환
//        return ResponseEntity.ok().header("JWT-Token", "Bearer " + newJwtToken).build();
//    }
//
//    // 리프레시 관리
//    @Autowired
//    private RestTemplate restTemplate;
//
//    // 외부 인증 제공자의 토큰 갱신 엔드포인트
//    private final String tokenEndpoint = "https://example.com/oauth2/token";
//
//    @PostMapping("/auth/token/refresh")
//    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
//        // 토큰 갱신을 위한 요청을 구성
//        // (실제 요청의 구조는 외부 인증 제공자에 따라 다를 수 있습니다)
//        String requestPayload = "grant_type=refresh_token&refresh_token=" + refreshToken;
//
//        // 외부 인증 제공자에게 토큰 갱신 요청을 전송
//        ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, requestPayload, String.class);
//
//        // 외부 인증 제공자로부터의 응답을 반환
//        return response;
//    }
//}
