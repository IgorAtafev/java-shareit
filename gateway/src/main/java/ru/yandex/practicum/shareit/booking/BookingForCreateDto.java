package ru.yandex.practicum.shareit.booking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.validator.StartDateBeforeEndDate;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@StartDateBeforeEndDate
public class BookingForCreateDto {

    @FutureOrPresent
    private LocalDateTime start;

    private LocalDateTime end;

    @NotNull(message = "ItemId must be null")
    private Long itemId;
}
