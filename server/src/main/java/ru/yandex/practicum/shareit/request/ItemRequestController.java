package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.shareit.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemRequestMapper itemRequestMapper;

    @GetMapping("/all")
    public List<ItemRequestDto> getItemRequestsAll(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("created").descending());

        List<ItemRequest> itemRequests = itemRequestService.getItemRequestsAll(userId, page);
        itemRequestService.setItemsToItemRequests(itemRequests);
        return itemRequestMapper.toDtos(itemRequests);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsByUserId(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId) {
        List<ItemRequest> itemRequests = itemRequestService.getItemRequestsByUserId(userId);
        itemRequestService.setItemsToItemRequests(itemRequests);
        return itemRequestMapper.toDtos(itemRequests);
    }

    @GetMapping("/{id}")
    public ItemRequestDto getItemRequestById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id
    ) {
        ItemRequest itemRequest = itemRequestService.getItemRequestById(id, userId);
        itemRequestService.setItemsToItemRequest(itemRequest);
        return itemRequestMapper.toDto(itemRequest);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.info("Request received POST /requests: '{}', userId: {}", itemRequestDto, userId);
        return itemRequestMapper.toDto(itemRequestService.createRequest(toItemRequest(itemRequestDto, userId)));
    }

    private ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long authorId) {
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(userService.getUserById(authorId));
        return itemRequest;
    }
}
