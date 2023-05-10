package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingForItemsMapper {

    public BookingForItemsDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingForItemsDto bookingDto = new BookingForItemsDto();

        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBookerId(booking.getBooker().getId());

        return bookingDto;
    }
}
