package damon.backend.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        // 로그인 실패 원인 로그 기록
        logger.error("OAuth2 Login failed", exception);

        // 실패 원인에 따라 다른 처리를 할 수 있습니다.
        if (exception.getMessage().contains("User is disabled")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your account is disabled.");
        } else if (exception.getMessage().contains("Invalid credentials")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials.");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 Authentication failed.");
        }
    }
}
