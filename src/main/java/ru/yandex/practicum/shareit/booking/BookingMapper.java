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

    public List<BookingForResponseDto> toDtos(Iterable<Booking> bookings) {
        List<BookingForResponseDto> bookingDtos = new ArrayList<>();

        for (Booking booking : bookings) {
            bookingDtos.add(toDto(booking));
        }

        return bookingDtos;
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
