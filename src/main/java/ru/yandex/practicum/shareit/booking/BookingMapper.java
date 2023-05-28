package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.item.ItemMapper;
import ru.yandex.practicum.shareit.user.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public BookingForResponseDto toDto(Booking booking) {
        BookingForResponseDto bookingDto = new BookingForResponseDto();

        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItem(itemMapper.toDto(booking.getItem()));
        bookingDto.setBooker(userMapper.toDto(booking.getBooker()));
        bookingDto.setStatus(booking.getStatus());

        return bookingDto;
    }

    public List<BookingForResponseDto> toDtos(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Booking toBooking(BookingForCreateDto bookingDto) {
        Booking booking = new Booking();

        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());

        return booking;
    }
}
