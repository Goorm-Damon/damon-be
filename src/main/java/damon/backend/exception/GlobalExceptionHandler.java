package damon.backend.exception;

import damon.backend.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "damon.backend")
public class GlobalExceptionHandler {
    //    @Trace
    @ExceptionHandler(NotMeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result notMeException(NotMeException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(NotFountException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result dataNotFound(NotFountException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(damon.backend.exception.BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result badRequest(org.apache.coyote.BadRequestException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(ForbbidenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result forbidden(ForbbidenException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result serverError(Exception e) {
        return Result.error("서버에서 알 수 없는 에러가 발생했습니다.");
    }
}
