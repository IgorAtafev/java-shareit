package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.booking.Booking;
import ru.yandex.practicum.shareit.booking.BookingForItemsDto;
import ru.yandex.practicum.shareit.booking.BookingForItemsMapper;
import ru.yandex.practicum.shareit.request.ItemRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    private final LocalDateTime currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);

    @Mock
    private BookingForItemsMapper bookingForItemsMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemMapper itemMapper;

    @Test
    void toDto_shouldReturnItemDto() {
        Item item = initItem();

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        BookingForItemsDto bookingDto1 = initBookingForItemsDto();
        BookingForItemsDto bookingDto2 = initBookingForItemsDto();

        item.setLastBooking(booking1);
        item.setNextBooking(booking2);

        when(bookingForItemsMapper.toDto(item.getLastBooking())).thenReturn(bookingDto1);
        when(bookingForItemsMapper.toDto(item.getNextBooking())).thenReturn(bookingDto2);

        Comment comment1 = initComment();
        Comment comment2 = initComment();
        CommentForResponseDto commentDto1 = initCommentForResponseDto();
        CommentForResponseDto commentDto2 = initCommentForResponseDto();

        List<Comment> comments = List.of(comment1, comment2);
        List<CommentForResponseDto> commentDtos = List.of(commentDto1, commentDto2);

        item.setComments(comments);

        when(commentMapper.toDtos(comments)).thenReturn(commentDtos);

        ItemDto itemDto = itemMapper.toDto(item);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Простая дрель");
        assertThat(itemDto.getAvailable()).isEqualTo(true);
        assertThat(itemDto.getLastBooking()).isEqualTo(bookingDto1);
        assertThat(itemDto.getNextBooking()).isEqualTo(bookingDto2);
        assertThat(itemDto.getComments()).isEqualTo(commentDtos);

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
    void toItem_shouldReturnItem() {
        ItemDto itemDto = initItemDto();

        Item item = itemMapper.toItem(itemDto);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Простая дрель");
        assertThat(item.getAvailable()).isEqualTo(true);
    }

    private ItemDto initItemDto() {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);
        itemDto.setComments(List.of());

        return itemDto;
    }

    private Item initItem() {
        Item item = new Item();

        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setComments(List.of());

        return item;
    }

    private CommentForResponseDto initCommentForResponseDto() {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setText("Комментарий пользователя");
        commentDto.setAuthorName("Автор");

        return commentDto;
    }

    private Comment initComment() {
        Comment comment = new Comment();
        comment.setText("Комментарий пользователя");
        return comment;
    }

    private BookingForItemsDto initBookingForItemsDto() {
        BookingForItemsDto bookingForItemsDto = new BookingForItemsDto();

        bookingForItemsDto.setStart(currentDateTime.plusHours(1));
        bookingForItemsDto.setEnd(currentDateTime.plusHours(2));

        return bookingForItemsDto;
    }

    private Booking initBooking() {
        Booking booking = new Booking();

        booking.setStart(currentDateTime.plusHours(1));
        booking.setEnd(currentDateTime.plusHours(2));

        return booking;
    }
}
