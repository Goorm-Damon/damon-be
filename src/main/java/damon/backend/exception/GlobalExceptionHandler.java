package damon.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<Object> handleReviewException(ReviewException ex) {
        // 에러 타입에 따라 적절한 HTTP 상태 코드와 메시지 설정
        HttpStatus status = switch (ex.getErrorType()) {
            case MEMBER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case REVIEW_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case COMMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

}
