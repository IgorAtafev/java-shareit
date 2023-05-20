package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemMapper;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {

    private final ItemService itemService;
    private final UserService userService;
    private final ItemMapper itemMapper;

    public ItemRequestDto toDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();

        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());

        return itemRequestDto;
    }

    public List<ItemRequestDto> toDtos(Collection<ItemRequest> itemRequests) {
        return  itemRequests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ItemRequestDto itemRequestWithItemsToDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = toDto(itemRequest);
        List<Item> items = itemService.getItemsByRequestId(itemRequestDto.getId());

        setItems(itemRequestDto, items);

        return itemRequestDto;
    }

    public List<ItemRequestDto> itemRequestWithItemsToDtos(Collection<ItemRequest> itemRequests) {
        List<ItemRequestDto> itemRequestsDto = toDtos(itemRequests);

        if (itemRequestsDto.isEmpty()) {
            return itemRequestsDto;
        }

        List<Long> itemRequestIds = itemRequestsDto.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> items = itemService.getItemsByRequestIds(itemRequestIds);

        for (ItemRequestDto itemRequestDto : itemRequestsDto) {
            setItems(itemRequestDto, items.get(itemRequestDto.getId()));
        }

        return itemRequestsDto;
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long authorId) {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(userService.getUserById(authorId));

        return itemRequest;
    }

    private void setItems(ItemRequestDto itemRequestDto, List<Item> items) {
        if (items != null) {
            itemRequestDto.setItems(itemMapper.toDtos(items));
        }
    }
}
