package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.item.ItemMapper;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.UserMapper;
import ru.yandex.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final ItemService itemService;
    private final UserService userService;

    public BookingForResponseDto toBookingDto(Booking booking) {
        BookingForResponseDto bookingDto = new BookingForResponseDto();

        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItem(itemMapper.toItemDto(booking.getItem()));
        bookingDto.setBooker(userMapper.toUserDto(booking.getBooker()));
        bookingDto.setStatus(booking.getStatus());

        return bookingDto;
    }

    public List<BookingForResponseDto> toBookingDto(Iterable<Booking> bookings) {
        List<BookingForResponseDto> bookingsDto = new ArrayList<>();

        for (Booking booking : bookings) {
            bookingsDto.add(toBookingDto(booking));
        }

        return bookingsDto;
    }

    public Booking toBooking(BookingForCreateDto bookingDto, Long ownerId) {
        Booking booking = new Booking();

        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(itemService.getItemById(bookingDto.getItemId()));
        booking.setBooker(userService.getUserById(ownerId));

        return booking;
    }
}
