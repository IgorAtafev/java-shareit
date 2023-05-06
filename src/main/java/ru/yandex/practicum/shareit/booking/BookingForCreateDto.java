package ru.yandex.practicum.shareit.booking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BookingForCreateDto {

    @NotNull(message = "Start date cannot be null")
    @Future
    private LocalDateTime start;

    @NotNull(message = "End date cannot be null")
    @Future
    private LocalDateTime end;

    @NotNull(message = "ItemId must be null")
    private Long itemId;
}
