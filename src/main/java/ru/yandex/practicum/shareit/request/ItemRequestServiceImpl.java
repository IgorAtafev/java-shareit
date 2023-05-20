package ru.yandex.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<ItemRequest> getItemRequestsAll(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        PageRequest page = PageRequest.of(from / size, size, Sort.by("created").descending());
        return itemRequestRepository.findByRequesterIdNot(userId, page).getContent();
    }

    @Override
    public Collection<ItemRequest> getItemRequestsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRequestRepository.findByRequesterId(userId, Sort.by("created").descending());
    }

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
}
