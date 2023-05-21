package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @GetMapping
    public List<ItemDto> getItemsByUserId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "20") @Min(1) Integer size
    ) {
        return itemMapper.itemWithBookingsAndCommentsToDtos(itemService.getItemsByUserId(userId, from, size));
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId, @PathVariable Long id) {
        Item item = itemService.getItemById(id);
        if (Objects.equals(userId, item.getOwner().getId())) {
            return itemMapper.itemWithBookingsAndCommentsToDto(item);
        }
        return itemMapper.itemWithCommentsToDto(item);
    }

    @PostMapping
    @Validated(ValidationOnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Valid ItemDto itemDto
    ) {
        log.info("Request received POST /items: '{}', userId: {}", itemDto, userId);
        return itemMapper.toDto(itemService.createItem(itemMapper.toItem(itemDto, userId)));
    }

    @PatchMapping("/{id}")
    public ItemDto updateItemById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestBody @Valid ItemDto itemDto
    ) {
        log.info("Request received PATCH /items/{}: '{}', userId: {}", id, itemDto, userId);
        itemDto.setId(id);
        return itemMapper.toDto(itemService.updateItem(itemMapper.toItem(itemDto, userId)));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "20") @Min(1) Integer size
    ) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemMapper.toDtos(itemService.searchItems(text, from, size));
    }

    @PostMapping("/{id}/comment")
    public CommentForResponseDto createComment(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestBody @Valid CommentForCreateDto commentDto
    ) {
        log.info("Request received POST /items/{}/comment: '{}', userId: {}", id, commentDto, userId);
        return commentMapper.toDto(
                itemService.createComment(commentMapper.toComment(commentDto, id, userId))
        );
    }
}
