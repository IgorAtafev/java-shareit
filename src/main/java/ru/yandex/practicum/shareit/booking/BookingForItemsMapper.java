package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingForItemsMapper {

    public BookingForItemsDto toBookingForItemsDto(Booking booking) {
        BookingForItemsDto bookingDto = new BookingForItemsDto();

        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBookerId(booking.getBooker().getId());

        return bookingDto;
    }

    public BookingForItemsDto getLastBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .reduce((booking1, booking2) -> booking2)
                .map(this::toBookingForItemsDto)
                .orElse(null);
    }

    public BookingForItemsDto getNextBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .findFirst()
                .map(this::toBookingForItemsDto)
                .orElse(null);
    }
}
