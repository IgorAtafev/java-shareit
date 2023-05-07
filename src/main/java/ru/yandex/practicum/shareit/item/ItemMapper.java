package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingForItemsMapper;
import ru.yandex.practicum.shareit.booking.BookingService;
import ru.yandex.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final BookingForItemsMapper bookingForItemsMapper;
    private final CommentMapper commentMapper;

    public ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());

        return itemDto;
    }

    public List<ItemDto> toItemDto(Collection<Item> items) {
        return  items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto toItemWithBookingsAndCommentsDto(Item item) {
        ItemDto itemDto = toItemDto(item);
        List<Booking> bookings = bookingService.getBookingsByItemId(itemDto.getId());
        List<Comment> comments = itemService.getCommentsByItemId(itemDto.getId());

        setLastAndNextBookings(itemDto, bookings);
        setComments(itemDto, comments);

        return itemDto;
    }

    public List<ItemDto> toItemWithBookingsAndCommentsDto(Collection<Item> items) {
        List<ItemDto> itemsDto = toItemDto(items);

        if (itemsDto.isEmpty()) {
            return itemsDto;
        }

        List<Long> itemIds = itemsDto.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<Booking>> bookings = bookingService.getBookingsByItemIds(itemIds);
        Map<Long, List<Comment>> comments = itemService.getCommentsByItemIds(itemIds);

        for (ItemDto itemDto : itemsDto) {
            setLastAndNextBookings(itemDto, bookings.get(itemDto.getId()));
            setComments(itemDto, comments.get(itemDto.getId()));
        }

        return itemsDto;
    }

    public ItemDto toItemWithCommentsDto(Item item) {
        ItemDto itemDto = toItemDto(item);
        List<Comment> comments = itemService.getCommentsByItemId(itemDto.getId());

        setComments(itemDto, comments);
        return itemDto;
    }

    public Item toItem(ItemDto itemDto, Long ownerId) {
        Item item = new Item();

        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());

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

        return item;
    }

    private void setLastAndNextBookings(ItemDto itemDto, List<Booking> bookings) {
       itemDto.setLastBooking(bookingForItemsMapper.getLastBooking(bookings));
       itemDto.setNextBooking(bookingForItemsMapper.getNextBooking(bookings));
    }

    private void setComments(ItemDto itemDto, List<Comment> comments) {
        if (comments != null) {
            itemDto.setComments(commentMapper.toCommentDto(comments));
        }
    }
}
