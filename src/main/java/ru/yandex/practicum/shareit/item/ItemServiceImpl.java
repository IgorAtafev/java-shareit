package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.booking.BookingRepository;
import ru.yandex.practicum.shareit.booking.BookingStatus;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Collection<Item> getItemsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRepository.findByOwnerIdOrderById(userId);
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Item with id %d does not exist", id)));
    }

    @Transactional
    @Override
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item updateItem(Item item) {
        if (!itemRepository.existsByIdAndOwnerId(item.getId(), item.getOwner().getId())) {
            throw new NotFoundException(String.format("Item with id %d and user id %d does not exist",
                    item.getId(), item.getOwner().getId()));
        }

        return itemRepository.save(item);
    }

    @Override
    public Collection<Item> searchItems(String text) {
        return itemRepository.searchItemsByText(text);
    }

    @Transactional
    @Override
    public Comment createComment(Comment comment) {
        Long userId = comment.getAuthor().getId();
        Long itemId = comment.getItem().getId();

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException(String.format("Item with id %d does not exist", itemId));
        }

        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())
        ) {
            throw new ValidationException(String.format(
                    "The user with id %d has not rented an item with id %d or the user's lease has not expired",
                    userId, itemId)
            );
        }

        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    public Map<Long, List<Comment>> getCommentsByItemIds(List<Long> itemIds) {
        return commentRepository.findByItemIdIn(itemIds, Sort.by("created").descending()).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
    }

    @Override
    public List<Comment> getCommentsByItemId(Long itemId) {
        return new ArrayList<>(commentRepository.findByItemId(itemId, Sort.by("created").descending()));
    }

    @Override
    public Map<Long, List<Item>> getItemsByRequestIds(List<Long> requestIds) {
        return itemRepository.findByRequestIdIn(requestIds, Sort.by("id").ascending()).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
    }

    @Override
    public List<Item> getItemsByRequestId(Long requestId) {
        return new ArrayList<>(itemRepository.findByRequestId(requestId, Sort.by("id").ascending()));
    }
}
