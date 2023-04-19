package ru.yandex.practicum.shareit.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleValidationException(final Exception e, final HttpServletResponse response) throws IOException {
        log.info(e.getMessage(), e);
        if (e.getMessage() != null) {
            response.getWriter().write(e.getMessage());
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFoundException(final NotFoundException e, final HttpServletResponse response)
            throws IOException {
        log.info(e.getMessage(), e);
        if (e.getMessage() != null) {
            response.getWriter().write(e.getMessage());
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleConflictException(final ConflictException e, final HttpServletResponse response)
            throws IOException {
        log.info(e.getMessage(), e);
        if (e.getMessage() != null) {
            response.getWriter().write(e.getMessage());
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleThrowable(final Throwable e, final HttpServletResponse response) throws IOException {
        log.info(e.getMessage(), e);
        if (e.getMessage() != null) {
            response.getWriter().write(e.getMessage());
        }
    }
}
