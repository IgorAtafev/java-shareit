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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @GetMapping
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemMapper.toItemWithBookingsAndCommentsDto(itemService.getItemsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        Item item = itemService.getItemById(id);
        if (Objects.equals(userId, item.getOwner().getId())) {
            return itemMapper.toItemWithBookingsAndCommentsDto(item);
        }
        return itemMapper.toItemWithCommentsDto(item);
    }

    @PostMapping
    @Validated(ValidationOnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemDto itemDto
    ) {
        log.info("Request received POST /items: '{}', userId: {}", itemDto, userId);
        return itemMapper.toItemDto(itemService.createItem(itemMapper.toItem(itemDto, userId)));
    }

    @PatchMapping("/{id}")
    public ItemDto updateItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody @Valid ItemDto itemDto
    ) {
        log.info("Request received PATCH /items/{}: '{}', userId: {}", id, itemDto, userId);
        itemDto.setId(id);
        return itemMapper.toItemDto(itemService.updateItem(itemMapper.toItem(itemDto, userId)));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemService.searchItems(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/comment")
    public CommentForResponseDto createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody @Valid CommentForCreateDto commentDto
    ) {
        log.info("Request received POST /items/{}/comment: '{}', userId: {}", id, commentDto, userId);
        return commentMapper.toCommentDto(
                itemService.createComment(commentMapper.toComment(commentDto, id, userId))
        );
    }
}
