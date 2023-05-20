package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.util.List;

@RestController
@RequestMapping("/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;

    @GetMapping("/all")
    public List<ItemRequestDto> getItemRequestsAll(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "20") @Min(1) Integer size
    ) {
        return itemRequestMapper.itemRequestWithItemsToDtos(itemRequestService.getItemRequestsAll(userId, from, size));
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsByUserId(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId) {
        return itemRequestMapper.itemRequestWithItemsToDtos(itemRequestService.getItemRequestsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ItemRequestDto getItemRequestById(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @PathVariable Long id
    ) {
        return itemRequestMapper.itemRequestWithItemsToDto(itemRequestService.getItemRequestById(id, userId));
    }

    @PostMapping
    @Validated(ValidationOnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestBody @Valid ItemRequestDto itemRequestDto
    ) {
        log.info("Request received POST /requests: '{}', userId: {}", itemRequestDto, userId);
        return itemRequestMapper.toDto(
                itemRequestService.createRequest(itemRequestMapper.toItemRequest(itemRequestDto, userId))
        );
    }
}
