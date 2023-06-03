package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequest> getItemRequestsAll(Long userId, Pageable page) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRequestRepository.findByRequestorIdNot(userId, page);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequest> getItemRequestsByUserId(Long userId) {
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
    public void setItemsToItemRequest(ItemRequest itemRequest) {
        List<Item> items = itemService.getItemsByRequestId(itemRequest.getId());
        setItems(itemRequest, items);
    }

    @Override
    public void setItemsToItemRequests(List<ItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return;
        }

        List<Long> itemRequestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> items = itemService.getItemsByRequestIds(itemRequestIds);

        itemRequests.forEach(itemRequest -> setItems(itemRequest, items.get(itemRequest.getId())));
    }

    private void setItems(ItemRequest itemRequest, List<Item> items) {
        if (items != null) {
            itemRequest.setItems(items);
        }
    }
}
