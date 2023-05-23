package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingForItemsMapper;
import ru.yandex.practicum.shareit.booking.BookingService;
import ru.yandex.practicum.shareit.request.ItemRequestService;
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
    private final ItemRequestService itemRequestService;
    private final BookingForItemsMapper bookingForItemsMapper;
    private final CommentMapper commentMapper;

    public ItemDto toDto(Item item) {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());

        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }

        return itemDto;
    }

    public List<ItemDto> toDtos(Collection<Item> items) {
        return  items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ItemDto itemWithBookingsAndCommentsToDto(Item item) {
        ItemDto itemDto = toDto(item);
        List<Booking> bookings = bookingService.getBookingsByItemId(itemDto.getId());
        List<Comment> comments = itemService.getCommentsByItemId(itemDto.getId());

        setLastAndNextBookings(itemDto, bookings);
        setComments(itemDto, comments);

        return itemDto;
    }

    public List<ItemDto> itemWithBookingsAndCommentsToDtos(Collection<Item> items) {
        List<ItemDto> itemDtos = toDtos(items);

        if (itemDtos.isEmpty()) {
            return itemDtos;
        }

        List<Long> itemIds = itemDtos.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<Booking>> bookings = bookingService.getBookingsByItemIds(itemIds);
        Map<Long, List<Comment>> comments = itemService.getCommentsByItemIds(itemIds);

        for (ItemDto itemDto : itemDtos) {
            setLastAndNextBookings(itemDto, bookings.get(itemDto.getId()));
            setComments(itemDto, comments.get(itemDto.getId()));
        }

        return itemDtos;
    }

    public ItemDto itemWithCommentsToDto(Item item) {
        ItemDto itemDto = toDto(item);
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

        if (itemDto.getRequestId() != null) {
            item.setRequest(itemRequestService.getItemRequestById(itemDto.getRequestId(), ownerId));
        }

        return item;
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
