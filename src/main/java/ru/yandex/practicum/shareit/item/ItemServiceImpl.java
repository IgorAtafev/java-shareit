package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingForItemsMapper;
import ru.yandex.practicum.shareit.booking.BookingRepository;
import ru.yandex.practicum.shareit.booking.BookingService;
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
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingForItemsMapper bookingForItemsMapper;
    private final BookingService bookingService;

    @Transactional(readOnly = true)
    @Override
    public Collection<Item> getItemsByUserId(Long userId, Pageable page) {
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
    public Collection<Item> searchItems(String text, Pageable page) {
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
    public List<ItemDto> itemWithBookingsAndCommentsToDtos(Collection<Item> items) {
        List<ItemDto> itemDtos = itemMapper.toDtos(items);

        if (itemDtos.isEmpty()) {
            return itemDtos;
        }

        List<Long> itemIds = itemDtos.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<Booking>> bookings = bookingService.getBookingsByItemIds(itemIds);
        Map<Long, List<Comment>> comments = getCommentsByItemIds(itemIds);

        for (ItemDto itemDto : itemDtos) {
            setLastAndNextBookings(itemDto, bookings.get(itemDto.getId()));
            setComments(itemDto, comments.get(itemDto.getId()));
        }

        return itemDtos;
    }

    @Override
    public ItemDto itemWithBookingsAndCommentsToDto(Item item) {
        ItemDto itemDto = itemMapper.toDto(item);
        List<Booking> bookings = bookingService.getBookingsByItemId(itemDto.getId());
        List<Comment> comments = getCommentsByItemId(itemDto.getId());

        setLastAndNextBookings(itemDto, bookings);
        setComments(itemDto, comments);

        return itemDto;
    }

    @Override
    public ItemDto itemWithCommentsToDto(Item item) {
        ItemDto itemDto = itemMapper.toDto(item);
        List<Comment> comments = getCommentsByItemId(itemDto.getId());

        setComments(itemDto, comments);
        return itemDto;
    }

    private void setLastAndNextBookings(ItemDto itemDto, List<Booking> bookings) {
        itemDto.setLastBooking(bookingForItemsMapper.toDto(bookingService.getLastBooking(bookings)));
        itemDto.setNextBooking(bookingForItemsMapper.toDto(bookingService.getNextBooking(bookings)));
    }

    private void setComments(ItemDto itemDto, List<Comment> comments) {
        if (comments != null) {
            itemDto.setComments(commentMapper.toDtos(comments));
        }
    }
}
