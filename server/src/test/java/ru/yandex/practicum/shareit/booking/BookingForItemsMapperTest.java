package ru.yandex.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingForItemsMapperTest {

    private final BookingForItemsMapper bookingMapper = new BookingForItemsMapper();

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        LocalDateTime currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);
        start = currentDateTime.plusHours(1);
        end = currentDateTime.plusHours(2);
    }

    @Test
    void toDto_shouldReturnBookingForItemsDto() {
        Booking booking = initBooking();

        BookingForItemsDto bookingDto = bookingMapper.toDto(booking);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(start);
        assertThat(bookingDto.getEnd()).isEqualTo(end);
        assertThat(bookingDto.getBookerId()).isEqualTo(2L);

        bookingDto = bookingMapper.toDto(null);
        assertThat(bookingDto).isNull();
    }

    private Booking initBooking() {
        Booking booking = new Booking();

        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(new User());
        booking.getBooker().setId(2L);

        return booking;
    }
}
