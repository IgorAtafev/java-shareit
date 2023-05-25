package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemMapper;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    private final ItemService itemService;

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemRequest> getItemRequestsAll(Long userId, Pageable page) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRequestRepository.findByRequestorIdNot(userId, page);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemRequest> getItemRequestsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRequestRepository.findByRequestorId(userId, Sort.by("created").descending());
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequest getItemRequestById(Long id, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRequestRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Item request with id %d does not exist", id)));
    }

    @Transactional
    @Override
    public ItemRequest createRequest(ItemRequest itemRequest) {
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public ItemRequestDto itemRequestWithItemsToDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);
        List<Item> items = itemService.getItemsByRequestId(itemRequestDto.getId());

        setItems(itemRequestDto, items);

        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> itemRequestWithItemsToDtos(Collection<ItemRequest> itemRequests) {
        List<ItemRequestDto> itemRequestDtos = itemRequestMapper.toDtos(itemRequests);

        if (itemRequestDtos.isEmpty()) {
            return itemRequestDtos;
        }

        List<Long> itemRequestIds = itemRequestDtos.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> items = itemService.getItemsByRequestIds(itemRequestIds);

        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            setItems(itemRequestDto, items.get(itemRequestDto.getId()));
        }

        return itemRequestDtos;
    }

    private void setItems(ItemRequestDto itemRequestDto, List<Item> items) {
        if (items != null) {
            itemRequestDto.setItems(itemMapper.toDtos(items));
        }
    }
}
