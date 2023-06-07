package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@Slf4j
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";
    private final BookingClient client;

    @GetMapping
    public ResponseEntity<Object> getBookingsByUserId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return client.getBookingsByUserId(userId, parameters);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByItemOwnerId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return client.getBookingsByItemOwnerId(userId, parameters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id
    ) {
        return client.getBookingById(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createBooking(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Valid BookingForCreateDto bookingDto
    ) {
        log.info("Request received POST /bookings: '{}', userId: {}", bookingDto, userId);
        return client.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> approveBookingById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestParam Boolean approved
    ) {
        log.info("Request received PATCH /bookings/{}: '{}', userId: {}", id, approved, userId);
        Map<String, Object> parameters = Map.of("approved", approved);
        return client.approveBookingById(userId, id, parameters);
    }
}
