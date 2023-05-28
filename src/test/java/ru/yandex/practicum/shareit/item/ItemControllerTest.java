package ru.yandex.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.shareit.request.ItemRequest;
import ru.yandex.practicum.shareit.request.ItemRequestService;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserService;
import ru.yandex.practicum.shareit.validator.ErrorHandler;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestService itemRequestService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void getItemsByUserId_shouldReturnEmptyListOfItems() throws Exception {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("id").ascending());

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1)).getItemsByUserId(userId, page);
    }

    @Test
    void getItemsByUserId_shouldReturnItemsByUserId() throws Exception {
        Long userId = 1L;
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("id").ascending());

        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        Item item1 = initItem();
        Item item2 = initItem();

        itemDto1.setId(itemId1);
        item1.setId(itemId1);
        itemDto2.setId(itemId2);
        item2.setId(itemId2);

        List<Item> expectedItem = List.of(item1, item2);
        List<ItemDto> expectedItemDto = List.of(itemDto1, itemDto2);

        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.getItemsByUserId(userId, page)).thenReturn(expectedItem);
        when(itemMapper.toDtos(expectedItem)).thenReturn(expectedItemDto);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemsByUserId(userId, page);
        verify(itemService, times(1)).setBookingsAndCommentsToItems(expectedItem);
        verify(itemMapper, times(1)).toDtos(expectedItem);
    }

    @Test
    void getItemsByUserId_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("id").ascending());

        when(itemService.getItemsByUserId(userId, page)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemsByUserId(userId, page);
    }

    @Test
    void getItemById_shouldReturnItemById_ifTheUserIsTheOwner() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        User user = initUser();
        ItemDto itemDto = initItemDto();
        Item item = initItem();

        user.setId(userId);
        item.setOwner(user);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.getItemById(itemId)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", itemId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemById(itemId);
        verify(itemService, times(1)).setBookingsAndCommentsToItem(item);
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    void getItemById_shouldReturnItemById_ifTheUserIsNotTheOwner() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        User user = initUser();
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        item.setOwner(user);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.getItemById(itemId)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", itemId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemById(itemId);
        verify(itemService, times(1)).setCommentsToItem(item);
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    void getItemById_shouldResponseWithNotFound_ifItemDoesNotExist() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        when(itemService.getItemById(itemId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/{id}", itemId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemById(itemId);
    }

    @Test
    void createItem_shouldResponseWithOk() throws Exception {
        Long userId = 1L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemService.createItem(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isCreated());

        verify(itemMapper, times(1)).toItem(itemDto);
        verify(itemService, times(1)).createItem(item);
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    void createItem_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemService.createItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemMapper, times(1)).toItem(itemDto);
        verify(itemService, times(1)).createItem(item);
        verify(itemMapper, never()).toDto(item);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItems")
    void createItem_shouldResponseWithBadRequest_ifTheItemIsInvalid(ItemDto itemDto) throws Exception {
        Long userId = 1L;

        String json = objectMapper.writeValueAsString(itemDto);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemById_shouldResponseWithOk() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();
        itemDto.setId(itemId);
        item.setId(itemId);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemService.updateItem(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isOk());

        verify(itemMapper, times(1)).toItem(itemDto);
        verify(itemService, times(1)).updateItem(item);
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    void updateItemById_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();
        itemDto.setId(itemId);
        item.setId(itemId);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemService.updateItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemMapper, times(1)).toItem(itemDto);
        verify(itemService, times(1)).updateItem(item);
        verify(itemMapper, never()).toDto(item);
    }

    @Test
    void updateItemById_shouldResponseWithNotFound_ifItemDoesNotExist() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        Long requestId = 2L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();
        Item oldItem = initItem();
        ItemRequest itemRequest = initItemRequest();
        itemDto.setId(itemId);
        item.setId(itemId);
        oldItem.setId(itemId);

        itemDto.setName(null);
        itemDto.setDescription(null);
        itemDto.setAvailable(null);
        itemDto.setRequestId(requestId);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemService.getItemById(itemId)).thenReturn(oldItem);
        when(itemRequestService.getItemRequestById(requestId, userId)).thenReturn(itemRequest);
        when(itemService.updateItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemMapper, times(1)).toItem(itemDto);
        verify(itemService, times(1)).getItemById(itemId);
        verify(itemService, times(1)).updateItem(item);
        verify(itemRequestService, times(1)).getItemRequestById(requestId, userId);
        verify(itemMapper, never()).toDto(item);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItems")
    void updateItemById_shouldResponseWithBadRequest_ifTheItemIsInvalid(ItemDto itemDto) throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        itemDto.setId(itemId);

        String json = objectMapper.writeValueAsString(itemDto);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_shouldReturnEmptyListOfItems_ifSearchTextIsEmpty() throws Exception {
        String text = "";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, never()).searchItems(text, page);
    }

    @Test
    void searchItems_shouldReturnEmptyListOfItems() throws Exception {
        String text = "аккумулятор";
        Integer size = 20;
        Pageable page = PageRequest.of(0, size);

        List<Item> expectedItem = List.of();
        List<ItemDto> expectedItemDto = List.of();
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text, page)).thenReturn(expectedItem);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text, page);
    }

    @Test
    void searchItems_shouldReturnItems() throws Exception {
        String text = "дрель";
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size);

        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        Item item1 = initItem();
        Item item2 = initItem();

        itemDto1.setId(itemId1);
        item1.setId(itemId1);
        itemDto2.setId(itemId2);
        item2.setId(itemId2);

        List<Item> expectedItem = List.of(item1, item2);
        List<ItemDto> expectedItemDto = List.of(itemDto1, itemDto2);

        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text, page)).thenReturn(expectedItem);
        when(itemMapper.toDtos(expectedItem)).thenReturn(expectedItemDto);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text, page);
        verify(itemMapper, times(1)).toDtos(expectedItem);
    }

    @Test
    void createComment_shouldResponseWithOk() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        CommentForCreateDto commentForCreateDto = initCommentForCreateDto();
        CommentForResponseDto commentForResponseDto = initCommentForResponseDto();
        Comment comment = initComment();

        String json = objectMapper.writeValueAsString(commentForCreateDto);

        when(commentMapper.toComment(commentForCreateDto)).thenReturn(comment);
        when(itemService.createComment(comment)).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentForResponseDto);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isOk());

        verify(commentMapper, times(1)).toComment(commentForCreateDto);
        verify(itemService, times(1)).createComment(comment);
        verify(commentMapper, times(1)).toDto(comment);
    }

    @Test
    void createComment_shouldResponseWithNotFound_ifUserOrItemDoesNotExist() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        CommentForCreateDto commentForCreateDto = initCommentForCreateDto();
        Comment comment = initComment();

        String json = objectMapper.writeValueAsString(commentForCreateDto);

        when(commentMapper.toComment(commentForCreateDto)).thenReturn(comment);
        when(itemService.createComment(comment)).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(commentMapper, times(1)).toComment(commentForCreateDto);
        verify(itemService, times(1)).createComment(comment);
        verify(commentMapper, never()).toDto(comment);
    }

    @Test
    void createComment_shouldResponseWithBadRequest_ifTheItemBookingIsNotValid() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        CommentForCreateDto commentForCreateDto = initCommentForCreateDto();
        Comment comment = initComment();

        String json = objectMapper.writeValueAsString(commentForCreateDto);

        when(commentMapper.toComment(commentForCreateDto)).thenReturn(comment);
        when(itemService.createComment(comment)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());

        verify(commentMapper, times(1)).toComment(commentForCreateDto);
        verify(itemService, times(1)).createComment(comment);
        verify(commentMapper, never()).toDto(comment);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidComments")
    void createComment_shouldResponseWithBadRequest_ifTheCommentIsInvalid(CommentForCreateDto commentDto)
            throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        String json = objectMapper.writeValueAsString(commentDto);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideInvalidItems() {
        return Stream.of(
                Arguments.of(initItemDto(dto -> dto.setName(""))),
                Arguments.of(initItemDto(dto -> dto.setName("Д"))),
                Arguments.of(initItemDto(dto -> dto.setName("Дрель".repeat(20) + "ь"))),
                Arguments.of(initItemDto(dto -> dto.setDescription(""))),
                Arguments.of(initItemDto(dto -> dto.setDescription("Д"))),
                Arguments.of(initItemDto(dto -> dto.setDescription("Дрель".repeat(40) + "ь")))
        );
    }

    private static Stream<Arguments> provideInvalidComments() {
        return Stream.of(
                Arguments.of(initCommentForCreateDto(dto -> dto.setText(null))),
                Arguments.of(initCommentForCreateDto(dto -> dto.setText(""))),
                Arguments.of(initCommentForCreateDto(dto -> dto.setText("     "))),
                Arguments.of(initCommentForCreateDto(dto -> dto.setText("К"))),
                Arguments.of(initCommentForCreateDto(dto -> dto.setText("Комме".repeat(200) + "н")))
        );
    }

    private static ItemDto initItemDto() {
        ItemDto itemDto = new ItemDto();

        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        return itemDto;
    }

    private static ItemDto initItemDto(Consumer<ItemDto> consumer) {
        ItemDto itemDto = initItemDto();
        consumer.accept(itemDto);
        return itemDto;
    }

    private static CommentForCreateDto initCommentForCreateDto() {
        CommentForCreateDto commentDto = new CommentForCreateDto();
        commentDto.setText("Комментарий пользователя");
        return commentDto;
    }

    private static CommentForCreateDto initCommentForCreateDto(Consumer<CommentForCreateDto> consumer) {
        CommentForCreateDto commentForCreateDto = initCommentForCreateDto();
        consumer.accept(commentForCreateDto);
        return commentForCreateDto;
    }

    private User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }

    private Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        return item;
    }

    private CommentForResponseDto initCommentForResponseDto() {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setText("Комментарий пользователя");
        commentDto.setAuthorName("Автор");
        commentDto.setCreated(LocalDateTime.now());

        return commentDto;
    }

    private Comment initComment() {
        Comment comment = new Comment();
        comment.setText("Комментарий пользователя");
        return comment;
    }

    private ItemRequest initItemRequest() {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setDescription("Хотел бы воспользоваться щеткой для обуви");
        itemRequest.setRequestor(new User());

        return itemRequest;
    }
}
