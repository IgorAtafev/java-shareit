package ru.yandex.practicum.shareit.item;

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
import ru.yandex.practicum.shareit.request.ItemRequestService;
import ru.yandex.practicum.shareit.user.UserService;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
    private final UserService userService;
    private final ItemRequestService itemRequestService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @GetMapping
    public List<ItemDto> getItemsByUserId(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Item> items = itemService.getItemsByUserId(userId, page);
        itemService.setBookingsAndCommentsToItems(items);
        return itemMapper.toDtos(items);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId, @PathVariable Long id) {
        Item item = itemService.getItemById(id);

        if (Objects.equals(userId, item.getOwner().getId())) {
            itemService.setBookingsAndCommentsToItem(item);
        } else {
            itemService.setCommentsToItem(item);
        }

        return itemMapper.toDto(item);
    }

    @PostMapping
    @Validated(ValidationOnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Valid ItemDto itemDto
    ) {
        log.info("Request received POST /items: '{}', userId: {}", itemDto, userId);
        return itemMapper.toDto(itemService.createItem(toItem(itemDto, userId)));
    }

    @PatchMapping("/{id}")
    public ItemDto updateItemById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestBody @Valid ItemDto itemDto
    ) {
        log.info("Request received PATCH /items/{}: '{}', userId: {}", id, itemDto, userId);
        itemDto.setId(id);
        return itemMapper.toDto(itemService.updateItem(toItem(itemDto, userId)));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "20") @Positive Integer size
    ) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        Pageable page = PageRequest.of(from / size, size);
        return itemMapper.toDtos(itemService.searchItems(text, page));
    }

    @PostMapping("/{id}/comment")
    public CommentForResponseDto createComment(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id,
            @RequestBody @Valid CommentForCreateDto commentDto
    ) {
        log.info("Request received POST /items/{}/comment: '{}', userId: {}", id, commentDto, userId);
        return commentMapper.toDto(itemService.createComment(toComment(commentDto, id, userId)));
    }

    private Item toItem(ItemDto itemDto, Long ownerId) {
        Item item = itemMapper.toItem(itemDto);

        if (itemDto.getId() != null) {
            Item oldItem = itemService.getItemById(itemDto.getId());

            if (itemDto.getName() == null) {
                item.setName(oldItem.getName());
            }
            if (itemDto.getDescription() == null) {
                item.setDescription(oldItem.getDescription());
            }
            if (itemDto.getAvailable() == null) {
                item.setAvailable(oldItem.getAvailable());
            }
        }

        item.setOwner(userService.getUserById(ownerId));

        if (itemDto.getRequestId() != null) {
            item.setRequest(itemRequestService.getItemRequestById(itemDto.getRequestId(), ownerId));
        }

        return item;
    }

    private Comment toComment(CommentForCreateDto commentDto, Long itemId, Long authorId) {
        Comment comment = commentMapper.toComment(commentDto);

        comment.setText(commentDto.getText());
        comment.setItem(itemService.getItemById(itemId));
        comment.setAuthor(userService.getUserById(authorId));

        return comment;
    }
}
