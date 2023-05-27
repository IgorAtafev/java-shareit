package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingRepository;
import ru.yandex.practicum.shareit.booking.BookingService;
import ru.yandex.practicum.shareit.booking.BookingStatus;
import ru.yandex.practicum.shareit.request.ItemRequest;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    private final LocalDateTime currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItemsByUserId_shouldReturnEmptyListOfItems() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("id").ascending());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByOwnerId(userId, page)).thenReturn(Collections.emptyList());

        assertThat(itemService.getItemsByUserId(userId, page)).isEmpty();

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRepository, times(1)).findByOwnerId(userId, page);
    }

    @Test
    void getItemsByUserId_shouldReturnItemsByUserId() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("id").ascending());

        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> expected = List.of(item1, item2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByOwnerId(userId, page)).thenReturn(expected);

        assertThat(itemService.getItemsByUserId(userId, page)).isEqualTo(expected);

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRepository, times(1)).findByOwnerId(userId, page);
    }

    @Test
    void getItemsByUserId_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("id").ascending());

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemService.getItemsByUserId(userId, page));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRepository, never()).findByOwnerId(userId, page);
    }

    @Test
    void getItemById_shouldReturnItemById() {
        Long itemId = 1L;

        Item item = initItem();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThat(itemService.getItemById(itemId)).isEqualTo(item);

        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void getItemById_shouldThrowAnException_ifItemDoesNotExist() {
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemService.getItemById(itemId));

        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void createItem_shouldCreateAItem() {
        Long userId = 1L;

        Item item = initItem();
        item.getOwner().setId(userId);

        when(itemRepository.save(item)).thenReturn(item);

        assertThat(itemService.createItem(item)).isEqualTo(item);

        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void updateItem_shouldUpdateTheItem() {
        Long userId = 1L;
        Long itemId = 2L;

        Item item = initItem();
        item.getOwner().setId(userId);
        item.setId(itemId);

        when(itemRepository.existsByIdAndOwnerId(itemId, userId)).thenReturn(true);
        when(itemRepository.save(item)).thenReturn(item);

        assertThat(itemService.updateItem(item)).isEqualTo(item);

        verify(itemRepository, times(1)).existsByIdAndOwnerId(itemId, userId);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void updateItem_shouldThrowAnException_ifItemDoesNotExist() {
        Long itemId = 1L;
        Long userId = 1L;

        Item item = initItem();
        item.getOwner().setId(userId);
        item.setId(itemId);

        when(itemRepository.existsByIdAndOwnerId(itemId, userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemService.updateItem(item));

        verify(itemRepository, times(1)).existsByIdAndOwnerId(itemId, userId);
        verify(itemRepository, never()).save(item);
    }

    @Test
    void searchItems_shouldReturnEmptyListOfItems() {
        String text = "аккумулятор";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size);

        when(itemRepository.searchItemsByText(text, page)).thenReturn(Collections.emptyList());

        assertThat(itemService.searchItems(text, page)).isEmpty();

        verify(itemRepository, times(1)).searchItemsByText(text, page);
    }

    @Test
    void searchItems_shouldReturnItems() {
        String text = "дрель";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size);

        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> expected = List.of(item1, item2);

        when(itemRepository.searchItemsByText(text, page)).thenReturn(expected);

        assertThat(itemService.searchItems(text, page)).isEqualTo(expected);

        verify(itemRepository, times(1)).searchItemsByText(text, page);
    }

    @Test
    void createComment_shouldCreateAComment() {
        Long userId = 1L;
        Long itemId = 2L;
        BookingStatus bookingStatus = BookingStatus.APPROVED;

        Comment comment = initComment();
        comment.getItem().setId(itemId);
        comment.getAuthor().setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                    itemId, userId, bookingStatus, currentDateTime)).thenReturn(true);
            when(commentRepository.save(comment)).thenReturn(comment);

            assertThat(itemService.createComment(comment)).isEqualTo(comment);
        }

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRepository, times(1)).existsById(itemId);
        verify(bookingRepository, times(1)).existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, bookingStatus, currentDateTime);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void createComment_shouldCreateAComment_ifUserDoesNotExist() {
        Long userId = 1L;
        Long itemId = 2L;

        Comment comment = initComment();
        comment.getItem().setId(itemId);
        comment.getAuthor().setId(userId);

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemService.createComment(comment));

        verify(userRepository, times(1)).existsById(userId);
        verify(commentRepository, never()).save(comment);
    }

    @Test
    void createComment_shouldCreateAComment_ifItemDoesNotExist() {
        Long userId = 1L;
        Long itemId = 2L;

        Comment comment = initComment();
        comment.getItem().setId(itemId);
        comment.getAuthor().setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemService.createComment(comment));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRepository, times(1)).existsById(itemId);
        verify(commentRepository, never()).save(comment);
    }

    @Test
    void createComment_shouldCreateAComment_ifTheItemBookingIsNotValid() {
        Long userId = 1L;
        Long itemId = 2L;
        BookingStatus bookingStatus = BookingStatus.APPROVED;

        Comment comment = initComment();
        comment.getItem().setId(itemId);
        comment.getAuthor().setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.existsById(itemId)).thenReturn(true);

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                    itemId, userId, bookingStatus, currentDateTime)).thenReturn(false);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> itemService.createComment(comment));
        }

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRepository, times(1)).existsById(itemId);
        verify(bookingRepository, times(1)).existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, bookingStatus, currentDateTime);
        verify(commentRepository, never()).save(comment);
    }

    @Test
    void getCommentsByItemIds_shouldReturnEmptyListOfComments() {
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        List<Long> itemIds = List.of(itemId1, itemId2);
        Sort sort = Sort.by("created").descending();

        when(commentRepository.findByItemIdIn(itemIds, sort)).thenReturn(Collections.emptyList());

        assertThat(itemService.getCommentsByItemIds(itemIds)).isEqualTo(Map.of());

        verify(commentRepository, times(1)).findByItemIdIn(itemIds, sort);
    }

    @Test
    void getCommentsByItemIds_shouldReturnCommentsByItemIds() {
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        List<Long> itemIds = List.of(itemId1, itemId2);
        Sort sort = Sort.by("created").descending();

        Comment comment1 = initComment();
        Comment comment2 = initComment();
        Comment comment3 = initComment();

        comment1.getItem().setId(itemId1);
        comment2.getItem().setId(itemId1);
        comment3.getItem().setId(itemId2);

        List<Comment> comments = List.of(comment1, comment2, comment3);
        Map<Long, List<Comment>> expected = Map.of(itemId1, List.of(comment1, comment2), itemId2, List.of(comment3));

        when(commentRepository.findByItemIdIn(itemIds, sort)).thenReturn(comments);

        assertThat(itemService.getCommentsByItemIds(itemIds)).isEqualTo(expected);

        verify(commentRepository, times(1)).findByItemIdIn(itemIds, sort);
    }

    @Test
    void getCommentsByItemId_shouldReturnEmptyListOfComments() {
        Long itemId = 1L;
        Sort sort = Sort.by("created").descending();

        when(commentRepository.findByItemId(itemId, sort)).thenReturn(Collections.emptyList());

        assertThat(itemService.getCommentsByItemId(itemId)).isEmpty();

        verify(commentRepository, times(1)).findByItemId(itemId, sort);
    }

    @Test
    void getCommentsByItemId_shouldReturnCommentsByItemId() {
        Long itemId = 1L;
        Sort sort = Sort.by("created").descending();

        Comment comment1 = initComment();
        Comment comment2 = initComment();

        comment1.getItem().setId(itemId);
        comment2.getItem().setId(itemId);

        List<Comment> expected = List.of(comment1, comment2);

        when(commentRepository.findByItemId(itemId, sort)).thenReturn(expected);

        assertThat(itemService.getCommentsByItemId(itemId)).isEqualTo(expected);

        verify(commentRepository, times(1)).findByItemId(itemId, sort);
    }

    @Test
    void getItemsByRequestIds_shouldReturnEmptyListOfItems() {
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        List<Long> requestIds = List.of(requestId1, requestId2);
        Sort sort = Sort.by("id").ascending();

        when(itemRepository.findByRequestIdIn(requestIds, sort)).thenReturn(Collections.emptyList());

        assertThat(itemService.getItemsByRequestIds(requestIds)).isEqualTo(Map.of());

        verify(itemRepository, times(1)).findByRequestIdIn(requestIds, sort);
    }

    @Test
    void getItemsByRequestIds_shouldReturnItemsByRequestIds() {
        Long requestId1 = 1L;
        Long requestId2 = 2L;
        List<Long> requestIds = List.of(requestId1, requestId2);
        Sort sort = Sort.by("id").ascending();

        Item item1 = initItem();
        Item item2 = initItem();
        Item item3 = initItem();

        item1.getRequest().setId(requestId1);
        item2.getRequest().setId(requestId1);
        item3.getRequest().setId(requestId2);

        List<Item> items = List.of(item1, item2, item3);
        Map<Long, List<Item>> expected = Map.of(requestId1, List.of(item1, item2), requestId2, List.of(item3));

        when(itemRepository.findByRequestIdIn(requestIds, sort)).thenReturn(items);

        assertThat(itemService.getItemsByRequestIds(requestIds)).isEqualTo(expected);

        verify(itemRepository, times(1)).findByRequestIdIn(requestIds, sort);
    }

    @Test
    void getItemsByRequestId_shouldReturnEmptyListOfItems() {
        Long requestId = 1L;
        Sort sort = Sort.by("id").ascending();

        when(itemRepository.findByRequestId(requestId, sort)).thenReturn(Collections.emptyList());

        assertThat(itemService.getItemsByRequestId(requestId)).isEmpty();

        verify(itemRepository, times(1)).findByRequestId(requestId, sort);
    }

    @Test
    void getItemsByRequestId_shouldReturnItemsByRequestId() {
        Long requestId = 1L;
        Sort sort = Sort.by("id").ascending();

        Item item1 = initItem();
        Item item2 = initItem();

        item1.getRequest().setId(requestId);
        item2.getRequest().setId(requestId);

        List<Item> expected = List.of(item1, item2);

        when(itemRepository.findByRequestId(requestId, sort)).thenReturn(expected);

        assertThat(itemService.getItemsByRequestId(requestId)).isEqualTo(expected);

        verify(itemRepository, times(1)).findByRequestId(requestId, sort);
    }

    @Test
    void setBookingsAndCommentsToItems_shouldReturnListOfItem() {
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        Sort sort = Sort.by("created").descending();

        Item item1 = initItem();
        Item item2 = initItem();
        item1.setId(itemId1);
        item2.setId(itemId2);

        List<Item> items = List.of(item1, item2);

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        Booking booking3 = initBooking();

        Comment comment1 = initComment();
        Comment comment2 = initComment();
        Comment comment3 = initComment();

        comment1.getItem().setId(itemId1);
        comment2.getItem().setId(itemId1);
        comment3.getItem().setId(itemId2);

        List<Long> itemIds = List.of(itemId1, itemId2);
        List<Booking> itemBookings1 = List.of(booking1, booking2);
        List<Booking> itemBookings2 = List.of(booking3);
        List<Comment> itemComments1 = List.of(comment1, comment2);
        List<Comment> itemComments2 = List.of(comment3);

        Map<Long, List<Booking>> bookings = Map.of(itemId1, itemBookings1, itemId2, itemBookings2);
        List<Comment> comments = List.of(comment1, comment2, comment3);

        when(bookingService.getBookingsByItemIds(itemIds)).thenReturn(bookings);
        when(commentRepository.findByItemIdIn(itemIds, sort)).thenReturn(comments);

        itemService.getCommentsByItemIds(itemIds);

        when(bookingService.getLastBooking(itemBookings1)).thenReturn(booking1);
        when(bookingService.getNextBooking(itemBookings1)).thenReturn(booking2);
        when(bookingService.getLastBooking(itemBookings2)).thenReturn(booking3);

        itemService.setBookingsAndCommentsToItems(items);

        assertThat(items.get(0).getLastBooking()).isEqualTo(booking1);
        assertThat(items.get(0).getNextBooking()).isEqualTo(booking2);
        assertThat(items.get(1).getLastBooking()).isEqualTo(booking3);
        assertThat(items.get(0).getComments()).isEqualTo(itemComments1);
        assertThat(items.get(1).getComments()).isEqualTo(itemComments2);
    }

    @Test
    void setBookingsAndCommentsToItem_shouldReturnItem() {
        Long itemId = 1L;
        Sort sort = Sort.by("created").descending();

        Item item = initItem();
        item.setId(itemId);

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();

        Comment comment1 = initComment();
        Comment comment2 = initComment();

        List<Booking> bookings = List.of(booking1, booking2);
        List<Comment> comments = List.of(comment1, comment2);

        when(bookingService.getBookingsByItemId(itemId)).thenReturn(bookings);
        when(bookingService.getLastBooking(bookings)).thenReturn(booking1);
        when(bookingService.getNextBooking(bookings)).thenReturn(booking2);
        when(commentRepository.findByItemId(itemId, sort)).thenReturn(comments);

        itemService.setBookingsAndCommentsToItem(item);

        assertThat(item.getLastBooking()).isEqualTo(booking1);
        assertThat(item.getNextBooking()).isEqualTo(booking2);
        assertThat(item.getComments()).isEqualTo(comments);

        verify(bookingService, times(1)).getBookingsByItemId(itemId);
        verify(bookingService, times(1)).getLastBooking(bookings);
        verify(bookingService, times(1)).getNextBooking(bookings);
        verify(commentRepository, times(1)).findByItemId(itemId, sort);
    }

    @Test
    void setCommentsToItem_shouldReturnItem() {
        Long itemId = 1L;
        Sort sort = Sort.by("created").descending();

        Item item = initItem();
        item.setId(itemId);

        Comment comment1 = initComment();
        Comment comment2 = initComment();

        List<Comment> comments = List.of(comment1, comment2);

        when(commentRepository.findByItemId(itemId, sort)).thenReturn(comments);

        itemService.setCommentsToItem(item);

        assertThat(item.getComments()).isEqualTo(comments);

        verify(commentRepository, times(1)).findByItemId(itemId, sort);
    }

    private Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setOwner(new User());
        item.setRequest(new ItemRequest());

        return item;
    }

    private Comment initComment() {
        Comment comment = new Comment();

        comment.setText("Комментарий пользователя");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        return comment;
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
}
