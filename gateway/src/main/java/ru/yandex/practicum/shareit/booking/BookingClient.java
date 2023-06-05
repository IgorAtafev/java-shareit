package ru.yandex.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.yandex.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    @Autowired
    public BookingClient(@Value("${shareit_server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/bookings"))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getBookingsByUserId(Long userId, Map<String, Object> parameters) {
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getBookingsByItemOwnerId(Long userId, Map<String, Object> parameters) {
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingForCreateDto bookingDto) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> approveBookingById(Long userId, Long id, Map<String, Object> parameters) {
        return patch("/" + id + "?approved={approved}", userId, parameters);
    }
}
