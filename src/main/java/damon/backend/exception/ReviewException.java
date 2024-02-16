package damon.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReviewException extends RuntimeException{
    // 에러 타입을 구분하는 열거형
    public enum ErrorType {
        MEMBER_NOT_FOUND,
        REVIEW_NOT_FOUND,
        COMMENT_NOT_FOUND,
//        UNAUTHORIZED,
        IMAGE_UPLOAD_FAILED, // 이미지 업로드 실패
        IMAGE_SIZE_EXCEEDED // 이미지 크기 초과
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

//    public static ReviewException unauthorized() {
//        return new ReviewException(ErrorType.UNAUTHORIZED, "접근 권한이 없습니다.");
//    }

    // 이미지 업로드 실패에 대한 팩토리 메서드
    public static ReviewException imageUploadFailed() {
        return new ReviewException(ErrorType.IMAGE_UPLOAD_FAILED, "이미지 업로드에 실패했습니다.");
    }

    // 이미지 크기 초과에 대한 팩토리 메서드
    public static ReviewException imageSizeExceeded() {
        return new ReviewException(ErrorType.IMAGE_SIZE_EXCEEDED, "허용된 이미지 크기를 초과했습니다.");
    }

    // 에러 타입을 반환하는 메소드
    public ErrorType getErrorType() {
        return errorType;
    }
}


