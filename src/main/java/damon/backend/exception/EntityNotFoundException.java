package damon.backend.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
        super("데이터를 찾을 수 없습니다.");
    }

    public EntityNotFoundException(String object, Long id) {
        super(object + " 데이터를 찾을 수 없습니다. id : " + id);
    }

    public EntityNotFoundException(String object, String id) {
        super(object + " 데이터를 찾을 수 없습니다. id : " + id);
    }
}
