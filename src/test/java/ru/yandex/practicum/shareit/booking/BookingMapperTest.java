package ru.yandex.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemDto;
import ru.yandex.practicum.shareit.item.ItemMapper;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserDto;
import ru.yandex.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    private LocalDateTime start;
    private LocalDateTime end;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BookingMapper bookingMapper;

    @BeforeEach
    void setUp() {
        LocalDateTime currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);
        start = currentDateTime.plusHours(1);
        end = currentDateTime.plusHours(2);
    }

    @Test
    void toDto_shouldReturnBookingForResponseDto() {
        Item item = initItem();
        ItemDto itemDto = initItemDto();
        User user = initUser();
        UserDto userDto = initUserDto();
        Booking booking = initBooking();

        booking.setItem(item);
        booking.setBooker(user);

        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(userMapper.toDto(user)).thenReturn(userDto);

        BookingForResponseDto bookingDto = bookingMapper.toDto(booking);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(start);
        assertThat(bookingDto.getEnd()).isEqualTo(end);
        assertThat(bookingDto.getItem()).isEqualTo(itemDto);
        assertThat(bookingDto.getBooker()).isEqualTo(userDto);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);

        verify(itemMapper, times(1)).toDto(item);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void toDtos_shouldReturnEmptyListOfBookingDtos() {
        assertThat(bookingMapper.toDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void toDtos_shouldReturnListOfBookingDtos() {
        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        BookingForResponseDto bookingDto1 = initBookingForResponseDto();
        BookingForResponseDto bookingDto2 = initBookingForResponseDto();

        List<BookingForResponseDto> expected = List.of(bookingDto1, bookingDto2);

        assertThat(bookingMapper.toDtos(List.of(booking1, booking2))).isEqualTo(expected);
    }

    @Test
    void toBooking_shouldReturnBooking() {
        BookingForCreateDto bookingDto = initBookingForCreateDto();

        Booking booking = bookingMapper.toBooking(bookingDto);

        assertThat(booking.getStart()).isEqualTo(start);
        assertThat(booking.getEnd()).isEqualTo(end);
    }

    private BookingForResponseDto initBookingForResponseDto() {
        BookingForResponseDto bookingDto = new BookingForResponseDto();

        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setStatus(BookingStatus.WAITING);

        return bookingDto;
    }

    private BookingForCreateDto initBookingForCreateDto() {
        BookingForCreateDto bookingDto = new BookingForCreateDto();

        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        return bookingDto;
    }

    private Booking initBooking() {
        Booking booking = new Booking();

        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }

    private ItemDto initItemDto() {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        return itemDto;
    }

    private Item initItem() {
        Item item = new Item();

        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        return item;
    }

    private UserDto initUserDto() {
        UserDto userDto = new UserDto();

        userDto.setId(1L);
        userDto.setEmail("user@user.com");
        userDto.setName("user");

        return userDto;
    }

    private User initUser() {
        User user = new User();

        user.setId(1L);
        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}
