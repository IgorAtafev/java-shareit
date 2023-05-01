package ru.yandex.practicum.shareit.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public void handleValidationException(final Exception e, final HttpServletResponse response) throws IOException {
        log.info(e.getMessage(), e);
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler
    public void handleNotFoundException(final NotFoundException e, final HttpServletResponse response)
            throws IOException {
        log.info(e.getMessage(), e);
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler
    public void handleThrowable(final Throwable e, final HttpServletResponse response) throws IOException {
        log.info(e.getMessage(), e);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }
}
