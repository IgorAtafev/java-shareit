package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.UserService;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@Slf4j
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    @GetMapping
    public List<BookingForResponseDto> getBookingsByUserId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        return bookingMapper.toDtos(bookingService.getBookingsByUserId(userId, state, page));
    }

    @GetMapping("/owner")
    public List<BookingForResponseDto> getBookingsByItemOwnerId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        return bookingMapper.toDtos(bookingService.getBookingsByItemOwnerId(userId, state, page));
    }

    @GetMapping("/{id}")
    public BookingForResponseDto getBookingById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id
    ) {
        return bookingMapper.toDto(bookingService.getBookingById(id, userId));
    }

    @PostMapping
    @Validated(ValidationOnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    public BookingForResponseDto createBooking(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Valid BookingForCreateDto bookingDto
    ) {
        log.info("Request received POST /bookings: '{}', userId: {}", bookingDto, userId);
        return bookingMapper.toDto(bookingService.createBooking(toBooking(bookingDto, userId)));
    }

    @PatchMapping("/{id}")
    public BookingForResponseDto approveBookingById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestParam Boolean approved
    ) {
        log.info("Request received PATCH /bookings/{}: '{}', userId: {}", id, approved, userId);
        return bookingMapper.toDto(bookingService.approveBookingById(id, approved, userId));
    }

    private Booking toBooking(BookingForCreateDto bookingDto, Long ownerId) {
        Booking booking = bookingMapper.toBooking(bookingDto);

        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(itemService.getItemById(bookingDto.getItemId()));
        booking.setBooker(userService.getUserById(ownerId));

        return booking;
    }
}
