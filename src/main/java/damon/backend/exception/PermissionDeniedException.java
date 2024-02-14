package damon.backend.exception;

public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException() {
        super("해당 사용자는 수정 권한이 없습니다.");
    }
}