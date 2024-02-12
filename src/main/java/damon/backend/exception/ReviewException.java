package damon.backend.exception;

public class ReviewException extends RuntimeException{
    // 에러 타입을 구분하는 열거형
    public enum ErrorType {
        MEMBER_NOT_FOUND,
        REVIEW_NOT_FOUND,
        COMMENT_NOT_FOUND,
        UNAUTHORIZED
    }

    private final ErrorType errorType;

    public ReviewException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    // 에러 타입에 따른 정적 팩토리 메소드
    public static ReviewException memberNotFound() {
        return new ReviewException(ErrorType.MEMBER_NOT_FOUND, "존재하지 않는 사용자입니다.");
    }

    public static ReviewException reviewNotFound() {
        return new ReviewException(ErrorType.REVIEW_NOT_FOUND, "존재하지 않는 리뷰입니다.");
    }

    public static ReviewException commentNotFound() {
        return new ReviewException(ErrorType.COMMENT_NOT_FOUND, "존재하지 않는 댓글입니다.");
    }

    public static ReviewException unauthorized() {
        return new ReviewException(ErrorType.UNAUTHORIZED, "접근 권한이 없습니다.");
    }

    // 에러 타입을 반환하는 메소드
    public ErrorType getErrorType() {
        return errorType;
    }
}


