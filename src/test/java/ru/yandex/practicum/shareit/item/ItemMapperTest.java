package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingForItemsDto;
import ru.yandex.practicum.shareit.booking.BookingForItemsMapper;
import ru.yandex.practicum.shareit.booking.BookingService;
import ru.yandex.practicum.shareit.booking.BookingStatus;
import ru.yandex.practicum.shareit.request.ItemRequest;
import ru.yandex.practicum.shareit.request.ItemRequestService;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    private LocalDateTime currentDateTime;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private ItemRequestService itemRequestService;

    @Mock
    private BookingForItemsMapper bookingForItemsMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemMapper itemMapper;

    @BeforeEach
    void setUp() {
        currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);
    }

    @Test
    void toDto_shouldReturnItemDto() {
        Item item = initItem();

        ItemDto itemDto = itemMapper.toDto(item);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Простая дрель");
        assertThat(itemDto.getAvailable()).isEqualTo(true);

        item.setRequest(new ItemRequest());
        item.getRequest().setId(2L);

        itemDto = itemMapper.toDto(item);

        assertThat(itemDto.getRequestId()).isEqualTo(2L);
    }

    @Test
    void toDtos_shouldReturnEmptyListOfItemDtos() {
        assertThat(itemMapper.toDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void toDtos_shouldReturnListOfItemDtos() {
        Item item1 = initItem();
        Item item2 = initItem();
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();

        List<ItemDto> expected = List.of(itemDto1, itemDto2);

        assertThat(itemMapper.toDtos(List.of(item1, item2))).isEqualTo(expected);
    }

    @Test
    void itemWithBookingsAndCommentsToDto_shouldReturnItemDto() {
        Item item = initItem();

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        BookingForItemsDto bookingDto1 = initBookingForItemsDto();
        BookingForItemsDto bookingDto2 = initBookingForItemsDto();

        Comment comment1 = initComment();
        Comment comment2 = initComment();
        CommentForResponseDto commentDto1 = initCommentForResponseDto();
        CommentForResponseDto commentDto2 = initCommentForResponseDto();

        List<Booking> bookings = List.of(booking1, booking2);
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentForResponseDto> commentDtos = List.of(commentDto1, commentDto2);

        when(bookingService.getBookingsByItemId(1L)).thenReturn(bookings);
        when(bookingService.getLastBooking(bookings)).thenReturn(booking1);
        when(bookingForItemsMapper.toDto(booking1)).thenReturn(bookingDto1);
        when(bookingService.getNextBooking(bookings)).thenReturn(booking2);
        when(bookingForItemsMapper.toDto(booking2)).thenReturn(bookingDto2);
        when(itemService.getCommentsByItemId(1L)).thenReturn(comments);
        when(commentMapper.toDtos(comments)).thenReturn(commentDtos);

        ItemDto itemDto = itemMapper.itemWithBookingsAndCommentsToDto(item);

        assertThat(itemDto.getLastBooking()).isEqualTo(bookingDto1);
        assertThat(itemDto.getNextBooking()).isEqualTo(bookingDto2);
        assertThat(itemDto.getComments()).isEqualTo(commentDtos);

        verify(bookingService, times(1)).getBookingsByItemId(1L);
        verify(bookingService, times(1)).getLastBooking(bookings);
        verify(bookingService, times(1)).getNextBooking(bookings);
        verify(itemService, times(1)).getCommentsByItemId(1L);
        verify(commentMapper, times(1)).toDtos(comments);

        comments = null;
        commentDtos = null;
        when(itemService.getCommentsByItemId(itemDto.getId())).thenReturn(comments);
        itemDto = itemMapper.itemWithBookingsAndCommentsToDto(item);

        assertThat(itemDto.getLastBooking()).isEqualTo(bookingDto1);
        assertThat(itemDto.getNextBooking()).isEqualTo(bookingDto2);
        assertThat(itemDto.getComments()).isEqualTo(commentDtos);
        verify(commentMapper, never()).toDtos(comments);
    }

    @Test
    void itemWithBookingsAndCommentsToDtos_shouldReturnEmptyListOfItemDto() {
        assertThat(itemMapper.itemWithBookingsAndCommentsToDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void itemWithBookingsAndCommentsToDtos_shouldReturnListOfItemDto() {
        Item item1 = initItem();
        Item item2 = initItem();
        item2.setId(2L);

        List<Item> items = List.of(item1, item2);

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        Booking booking3 = initBooking();
        BookingForItemsDto bookingDto1 = initBookingForItemsDto();
        BookingForItemsDto bookingDto2 = initBookingForItemsDto();
        BookingForItemsDto bookingDto3 = initBookingForItemsDto();

        Comment comment1 = initComment();
        Comment comment2 = initComment();
        Comment comment3 = initComment();
        CommentForResponseDto commentDto1 = initCommentForResponseDto();
        CommentForResponseDto commentDto2 = initCommentForResponseDto();
        CommentForResponseDto commentDto3 = initCommentForResponseDto();

        List<Long> itemIds = List.of(1L, 2L);
        List<Booking> itemBookings1 = List.of(booking1, booking2);
        List<Booking> itemBookings2 = List.of(booking3);
        List<Comment> itemComments1 = List.of(comment1, comment2);
        List<Comment> itemComments2 = List.of(comment3);
        List<CommentForResponseDto> itemCommentDtos1 = List.of(commentDto1, commentDto2);
        List<CommentForResponseDto> itemCommentDtos2 = List.of(commentDto3);

        Map<Long, List<Booking>> bookings = Map.of(1L, itemBookings1, 2L, itemBookings2);
        Map<Long, List<Comment>> comments = Map.of(1L, itemComments1, 2L, itemComments2);

        when(bookingService.getBookingsByItemIds(itemIds)).thenReturn(bookings);
        when(itemService.getCommentsByItemIds(itemIds)).thenReturn(comments);
        when(bookingService.getLastBooking(itemBookings1)).thenReturn(booking1);
        when(bookingForItemsMapper.toDto(booking1)).thenReturn(bookingDto1);
        when(bookingService.getNextBooking(itemBookings1)).thenReturn(booking2);
        when(bookingForItemsMapper.toDto(booking2)).thenReturn(bookingDto2);
        when(bookingService.getLastBooking(itemBookings2)).thenReturn(booking3);
        when(bookingForItemsMapper.toDto(booking3)).thenReturn(bookingDto3);
        when(commentMapper.toDtos(itemComments1)).thenReturn(itemCommentDtos1);
        when(commentMapper.toDtos(itemComments2)).thenReturn(itemCommentDtos2);

        List<ItemDto> itemDtos = itemMapper.itemWithBookingsAndCommentsToDtos(items);

        assertThat(itemDtos.get(0).getLastBooking()).isEqualTo(bookingDto1);
        assertThat(itemDtos.get(0).getNextBooking()).isEqualTo(bookingDto2);
        assertThat(itemDtos.get(1).getLastBooking()).isEqualTo(bookingDto3);
        assertThat(itemDtos.get(0).getComments()).isEqualTo(itemCommentDtos1);
        assertThat(itemDtos.get(1).getComments()).isEqualTo(itemCommentDtos2);
    }

    @Test
    void itemWithCommentsToDto_shouldReturnItemDto() {
        Item item = initItem();

        Comment comment1 = initComment();
        Comment comment2 = initComment();
        CommentForResponseDto commentDto1 = initCommentForResponseDto();
        CommentForResponseDto commentDto2 = initCommentForResponseDto();

        List<Comment> comments = List.of(comment1, comment2);
        List<CommentForResponseDto> commentDtos = List.of(commentDto1, commentDto2);

        when(itemService.getCommentsByItemId(1L)).thenReturn(comments);
        when(commentMapper.toDtos(comments)).thenReturn(commentDtos);

        ItemDto itemDto = itemMapper.itemWithCommentsToDto(item);

        assertThat(itemDto.getComments()).isEqualTo(commentDtos);

        verify(itemService, times(1)).getCommentsByItemId(1L);
        verify(commentMapper, times(1)).toDtos(comments);

        comments = null;
        commentDtos = null;
        when(itemService.getCommentsByItemId(1L)).thenReturn(comments);
        itemDto = itemMapper.itemWithCommentsToDto(item);

        assertThat(itemDto.getComments()).isEqualTo(commentDtos);
        verify(commentMapper, never()).toDtos(comments);
    }

    @Test
    void toItem_shouldReturnItem() {
        Long ownerId = 1L;
        Long requestId = 2L;
        ItemDto itemDto = initItemDto();
        itemDto.setId(null);
        itemDto.setRequestId(requestId);

        User owner = initUser();
        owner.setId(ownerId);

        ItemRequest itemRequest = initItemRequest();
        itemRequest.setId(requestId);

        when(userService.getUserById(ownerId)).thenReturn(owner);
        when(itemRequestService.getItemRequestById(requestId, ownerId)).thenReturn(itemRequest);

        Item item = itemMapper.toItem(itemDto, ownerId);

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Простая дрель");
        assertThat(item.getAvailable()).isEqualTo(true);
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isEqualTo(itemRequest);

        verify(userService, times(1)).getUserById(ownerId);
        verify(itemRequestService, times(1)).getItemRequestById(requestId, ownerId);

        Long itemId = 1L;
        String updatedName = "Аккумулятор";
        String updatedDescription = "Аккумулятор";
        Boolean updatedAvailable = false;

        itemDto.setId(itemId);
        itemDto.setName(updatedName);
        itemDto.setDescription(updatedDescription);
        itemDto.setAvailable(updatedAvailable);

        Item oldItem = initItem();
        oldItem.setId(itemId);

        when(itemService.getItemById(itemId)).thenReturn(oldItem);

        item = itemMapper.toItem(itemDto, ownerId);

        assertThat(item.getName()).isEqualTo(updatedName);
        assertThat(item.getDescription()).isEqualTo(updatedDescription);
        assertThat(item.getAvailable()).isEqualTo(updatedAvailable);

        verify(itemService, times(1)).getItemById(itemId);

        itemDto.setName(null);
        item = itemMapper.toItem(itemDto, ownerId);
        assertThat(item.getName()).isEqualTo("Дрель");

        itemDto.setDescription(null);
        item = itemMapper.toItem(itemDto, ownerId);
        assertThat(item.getDescription()).isEqualTo("Простая дрель");

        itemDto.setAvailable(null);
        item = itemMapper.toItem(itemDto, ownerId);
        assertThat(item.getAvailable()).isEqualTo(true);
    }

    private ItemDto initItemDto() {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        return itemDto;
    }

    private Item initItem() {
        Item item = new Item();

        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setOwner(new User());

        return item;
    }

    private BookingForItemsDto initBookingForItemsDto() {
        BookingForItemsDto bookingForItemsDto = new BookingForItemsDto();

        bookingForItemsDto.setId(1L);
        bookingForItemsDto.setStart(currentDateTime.plusHours(1));
        bookingForItemsDto.setEnd(currentDateTime.plusHours(2));
        bookingForItemsDto.setBookerId(2L);

        return bookingForItemsDto;
    }

    private Booking initBooking() {
        Booking booking = new Booking();

        booking.setId(1L);
        booking.setStart(currentDateTime.plusHours(1));
        booking.setEnd(currentDateTime.plusHours(2));
        booking.setItem(new Item());
        booking.getItem().setOwner(new User());
        booking.setBooker(new User());
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }

    private CommentForResponseDto initCommentForResponseDto() {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setId(1L);
        commentDto.setText("Комментарий пользователя");
        commentDto.setAuthorName("Автор");
        commentDto.setCreated(currentDateTime);

        return commentDto;
    }

    private Comment initComment() {
        Comment comment = new Comment();

        comment.setId(1L);
        comment.setText("Комментарий пользователя");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        return comment;
    }

    private User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }

    private ItemRequest initItemRequest() {
        ItemRequest itemRequest = new ItemRequest();
        return itemRequest;
    }
}
