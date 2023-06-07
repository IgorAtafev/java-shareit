package ru.yandex.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.booking.BookingForItemsDto;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingForItemsDto lastBooking;

    private BookingForItemsDto nextBooking;

    private List<CommentForResponseDto> comments;

    private Long requestId;
}
