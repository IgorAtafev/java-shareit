package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;

@Controller
@RequestMapping("/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient client;

    @GetMapping("/all")
    public ResponseEntity<Object> getItemRequestsAll(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return client.getItemRequestsAll(userId, parameters);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsByUserId(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId) {
        return client.getItemRequestsByUserId(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemRequestById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id
    ) {
        return client.getItemRequestById(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createRequest(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Validated(ValidationOnCreate.class) ItemRequestDto itemRequestDto
    ) {
        log.info("Request received POST /requests: '{}', userId: {}", itemRequestDto, userId);
        return client.createRequest(userId, itemRequestDto);
    }
}
