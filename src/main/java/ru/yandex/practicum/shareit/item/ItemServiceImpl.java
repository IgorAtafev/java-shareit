package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingRepository;
import ru.yandex.practicum.shareit.booking.BookingService;
import ru.yandex.practicum.shareit.booking.BookingStatus;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;

    @Transactional(readOnly = true)
    @Override
    public List<Item> getItemsByUserId(Long userId, Pageable page) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRepository.findByOwnerId(userId, page);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public List<Item> searchItems(String text, Pageable page) {
        return itemRepository.searchItemsByText(text, page);
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
    public Map<Long, List<Item>> getItemsByRequestIds(List<Long> requestIds) {
        return itemRepository.findByRequestIdIn(requestIds, Sort.by("id").ascending()).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
    }

    @Override
    public List<Item> getItemsByRequestId(Long requestId) {
        return new ArrayList<>(itemRepository.findByRequestId(requestId, Sort.by("id").ascending()));
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
    public void setBookingsAndCommentsToItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        Map<Long, List<Booking>> bookings = bookingService.getBookingsByItemIds(itemIds);
        Map<Long, List<Comment>> comments = getCommentsByItemIds(itemIds);

        for (Item item : items) {
            setLastAndNextBookings(item, bookings.get(item.getId()));
            setComments(item, comments.get(item.getId()));
        }
    }

    @Override
    public void setBookingsAndCommentsToItem(Item item) {
        List<Booking> bookings = bookingService.getBookingsByItemId(item.getId());
        List<Comment> comments = getCommentsByItemId(item.getId());

        setLastAndNextBookings(item, bookings);
        setComments(item, comments);
    }

    @Override
    public void setCommentsToItem(Item item) {
        List<Comment> comments = getCommentsByItemId(item.getId());
        setComments(item, comments);
    }

    private void setLastAndNextBookings(Item item, List<Booking> bookings) {
        item.setLastBooking(bookingService.getLastBooking(bookings));
        item.setNextBooking(bookingService.getNextBooking(bookings));
    }

    private void setComments(Item item, List<Comment> comments) {
        if (comments != null) {
            item.setComments(comments);
        }
    }
}
