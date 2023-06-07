package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;
import ru.yandex.practicum.shareit.validator.ValidationOnUpdate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";
    private final ItemClient client;

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return client.getItemsByUserId(userId, parameters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id
    ) {
        return client.getItemById(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createItem(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Validated(ValidationOnCreate.class) ItemDto itemDto
    ) {
        log.info("Request received POST /items: '{}', userId: {}", itemDto, userId);
        return client.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItemById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestBody @Validated(ValidationOnUpdate.class) ItemDto itemDto
    ) {
        log.info("Request received PATCH /items/{}: '{}', userId: {}", id, itemDto, userId);
        return client.updateItemById(userId, id, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        if (text.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return client.searchItems(parameters);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestBody @Validated(ValidationOnCreate.class) CommentForCreateDto commentDto
    ) {
        log.info("Request received POST /items/{}/comment: '{}', userId: {}", id, commentDto, userId);
        return client.createComment(userId, id, commentDto);
    }
}
