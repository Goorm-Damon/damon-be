package damon.backend.exception;

public class NotMeException extends RuntimeException {

    public NotMeException() {
        super("인증되지 않은 사용자입니다.");
    }
}