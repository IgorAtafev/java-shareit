package ru.yandex.practicum.shareit.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Checks that the start date must be before the end date.
 * Null elements are considered invalid.
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookingDateValidator.class)
@Documented
public @interface StartDateBeforeEndDate {

    String message() default "The start date must be before the end date or not null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
