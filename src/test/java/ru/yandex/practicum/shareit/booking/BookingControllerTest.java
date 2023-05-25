package ru.yandex.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserService;
import ru.yandex.practicum.shareit.validator.ErrorHandler;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void getBookingsByUserId_shouldReturnEmptyListOfBookings() throws Exception {
        Long userId = 1L;
        String state = "ALL";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        mockMvc.perform(get("/bookings?state={state}", state).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bookingService, times(1)).getBookingsByUserId(userId, state, page);
    }

    @Test
    void getBookingsByUserId_shouldReturnBookingsByUserId() throws Exception {
        Long userId = 1L;
        Long bookingId1 = 1L;
        Long bookingId2 = 2L;
        String state = "ALL";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        BookingForResponseDto bookingDto1 = initBookingForResponseDto();
        BookingForResponseDto bookingDto2 = initBookingForResponseDto();
        Booking booking1 = initBooking();
        Booking booking2 = initBooking();

        bookingDto1.setId(bookingId1);
        booking1.setId(bookingId1);
        bookingDto2.setId(bookingId2);
        booking2.setId(bookingId2);

        List<Booking> expectedBooking = List.of(booking1, booking2);
        List<BookingForResponseDto> expectedBookingDto = List.of(bookingDto1, bookingDto2);

        String json = objectMapper.writeValueAsString(expectedBookingDto);

        when(bookingService.getBookingsByUserId(userId, state, page)).thenReturn(expectedBooking);
        when(bookingMapper.toDtos(expectedBooking)).thenReturn(expectedBookingDto);

        mockMvc.perform(get("/bookings?state={state}", state).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(bookingService, times(1)).getBookingsByUserId(userId, state, page);
        verify(bookingMapper, times(1)).toDtos(expectedBooking);
    }

    @Test
    void getBookingsByUserId_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;
        String state = "ALL";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        when(bookingService.getBookingsByUserId(userId, state, page)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/bookings?state={state}", state).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingsByUserId(userId, state, page);
    }

    @Test
    void getBookingsByItemOwnerId_shouldReturnEmptyListOfBookings() throws Exception {
        Long userId = 1L;
        String state = "ALL";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        mockMvc.perform(get("/bookings/owner?state={state}", state).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bookingService, times(1)).getBookingsByItemOwnerId(userId, state, page);
    }

    @Test
    void getBookingsByItemOwnerId_shouldReturnBookingsByItemOwnerId() throws Exception {
        Long userId = 1L;
        Long bookingId1 = 1L;
        Long bookingId2 = 2L;
        String state = "ALL";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        BookingForResponseDto bookingDto1 = initBookingForResponseDto();
        BookingForResponseDto bookingDto2 = initBookingForResponseDto();
        Booking booking1 = initBooking();
        Booking booking2 = initBooking();

        bookingDto1.setId(bookingId1);
        booking1.setId(bookingId1);
        bookingDto2.setId(bookingId2);
        booking2.setId(bookingId2);

        List<Booking> expectedBooking = List.of(booking1, booking2);
        List<BookingForResponseDto> expectedBookingDto = List.of(bookingDto1, bookingDto2);

        String json = objectMapper.writeValueAsString(expectedBookingDto);

        when(bookingService.getBookingsByItemOwnerId(userId, state, page)).thenReturn(expectedBooking);
        when(bookingMapper.toDtos(expectedBooking)).thenReturn(expectedBookingDto);

        mockMvc.perform(get("/bookings/owner?state={state}", state).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(bookingService, times(1)).getBookingsByItemOwnerId(userId, state, page);
        verify(bookingMapper, times(1)).toDtos(expectedBooking);
    }

    @Test
    void getBookingsByItemOwnerId_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;
        String state = "ALL";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        when(bookingService.getBookingsByItemOwnerId(userId, state, page)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/bookings/owner?state={state}", state).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingsByItemOwnerId(userId, state, page);
    }

    @Test
    void getBookingById_shouldReturnBookingById_ifTheUserIsTheOwnerOfTheItem() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;

        User user = initUser();
        user.setId(userId);
        BookingForResponseDto bookingDto = initBookingForResponseDto();
        Booking booking = initBooking();
        booking.getItem().setOwner(user);

        String json = objectMapper.writeValueAsString(bookingDto);

        when(bookingService.getBookingById(bookingId, userId)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{id}", bookingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(bookingService, times(1)).getBookingById(bookingId, userId);
        verify(bookingMapper, times(1)).toDto(booking);
    }

    @Test
    void getBookingById_shouldReturnBookingById_ifTheUserIsABooker() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;

        User user = initUser();
        user.setId(userId);
        BookingForResponseDto bookingDto = initBookingForResponseDto();
        Booking booking = initBooking();
        booking.setBooker(user);

        String json = objectMapper.writeValueAsString(bookingDto);

        when(bookingService.getBookingById(bookingId, userId)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{id}", bookingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(bookingService, times(1)).getBookingById(bookingId, userId);
        verify(bookingMapper, times(1)).toDto(booking);
    }

    @Test
    void getBookingById_shouldResponseWithNotFound_ifBookingDoesNotExist() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;

        when(bookingService.getBookingById(bookingId, userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/bookings/{id}", bookingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingById(bookingId, userId);
    }

    @Test
    void createBooking_shouldResponseWithOk() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        BookingForCreateDto bookingForCreateDto = initBookingForCreateDto();
        bookingForCreateDto.setItemId(itemId);
        BookingForResponseDto bookingForResponseDto = initBookingForResponseDto();
        Booking booking = initBooking();
        booking.getItem().setId(itemId);

        String json = objectMapper.writeValueAsString(bookingForCreateDto);

        when(bookingMapper.toBooking(bookingForCreateDto)).thenReturn(booking);
        when(bookingService.createBooking(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingForResponseDto);

        mockMvc.perform(post("/bookings").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isCreated());

        verify(bookingMapper, times(1)).toBooking(bookingForCreateDto);
        verify(bookingService, times(1)).createBooking(booking);
        verify(bookingMapper, times(1)).toDto(booking);
    }

    @Test
    void createBooking_shouldResponseWithNotFound_ifUserOrItemDoesNotExist() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        BookingForCreateDto bookingDto = initBookingForCreateDto();
        bookingDto.setItemId(itemId);
        Booking booking = initBooking();
        booking.getItem().setId(itemId);

        String json = objectMapper.writeValueAsString(bookingDto);

        when(bookingMapper.toBooking(bookingDto)).thenReturn(booking);
        when(bookingService.createBooking(booking)).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/bookings").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(bookingMapper, times(1)).toBooking(bookingDto);
        verify(bookingService, times(1)).createBooking(booking);
        verify(bookingMapper, never()).toDto(booking);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBookings")
    void createBooking_shouldResponseWithBadRequest_ifTheBookingIsInvalid(BookingForCreateDto bookingDto)
            throws Exception {
        Long userId = 1L;

        String json = objectMapper.writeValueAsString(bookingDto);

        mockMvc.perform(post("/bookings").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBookingById_shouldResponseWithOk() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;
        Boolean approved = true;

        BookingForResponseDto bookingDto = initBookingForResponseDto();
        Booking booking = initBooking();
        bookingDto.setId(bookingId);
        booking.setId(bookingId);

        String json = objectMapper.writeValueAsString(bookingDto);

        when(bookingService.approveBookingById(bookingId, approved, userId)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{id}?approved={approved}", bookingId, approved)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(bookingService, times(1)).approveBookingById(bookingId, approved, userId);
        verify(bookingMapper, times(1)).toDto(booking);
    }

    @Test
    void approveBookingById_shouldResponseWithNotFound_ifBookingDoesNotExist() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;
        Boolean approved = true;

        when(bookingService.approveBookingById(bookingId, approved, userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/bookings/{id}?approved={approved}", bookingId, approved)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).approveBookingById(bookingId, approved, userId);
    }

    private static Stream<Arguments> provideInvalidBookings() {
        LocalDateTime pastDateTime = LocalDateTime.now().minusHours(1);

        return Stream.of(
                Arguments.of(initBookingForCreateDto(dto -> dto.setStart(null))),
                Arguments.of(initBookingForCreateDto(dto -> dto.setStart(pastDateTime))),
                Arguments.of(initBookingForCreateDto(dto -> dto.setEnd(null))),
                Arguments.of(initBookingForCreateDto(dto -> dto.setEnd(pastDateTime))),
                Arguments.of(initBookingForCreateDto(dto -> dto.setItemId(null)))
        );
    }

    private static BookingForCreateDto initBookingForCreateDto(Consumer<BookingForCreateDto> consumer) {
        BookingForCreateDto bookingDto = initBookingForCreateDto();
        consumer.accept(bookingDto);
        return bookingDto;
    }

    private static User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }

    private static BookingForCreateDto initBookingForCreateDto() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        BookingForCreateDto bookingDto = new BookingForCreateDto();

        bookingDto.setStart(currentDateTime.plusHours(1));
        bookingDto.setEnd(currentDateTime.plusHours(2));

        return bookingDto;
    }

    private static BookingForResponseDto initBookingForResponseDto() {
        return new BookingForResponseDto();
    }

    private static Booking initBooking() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Booking booking = new Booking();

        booking.setStart(currentDateTime.plusHours(1));
        booking.setEnd(currentDateTime.plusHours(2));
        booking.setItem(new Item());

        return booking;
    }
}
