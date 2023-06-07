package ru.yandex.practicum.shareit.validator;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
