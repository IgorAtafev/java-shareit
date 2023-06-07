package ru.yandex.practicum.shareit.validator;

import ru.yandex.practicum.shareit.booking.BookingForCreateDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class BookingDateValidator implements ConstraintValidator<StartDateBeforeEndDate, BookingForCreateDto> {

    @Override
    public void initialize(StartDateBeforeEndDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(BookingForCreateDto bookingDto, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start == null || end == null) {
            return false;
        }

        return start.isBefore(end);
    }
}
