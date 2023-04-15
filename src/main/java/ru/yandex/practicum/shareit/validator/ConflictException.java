package ru.yandex.practicum.shareit.validator;

public class ConflictException extends RuntimeException {

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }
}