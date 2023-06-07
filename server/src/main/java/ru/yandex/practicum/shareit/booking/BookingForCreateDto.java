package ru.yandex.practicum.shareit.booking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BookingForCreateDto {

    private LocalDateTime start;

    private LocalDateTime end;

    private Long itemId;
}
