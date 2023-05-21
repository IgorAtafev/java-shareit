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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.shareit.user.User;
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
        Integer from = 0;
        Integer size = 20;

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1)).getItemsByUserId(userId, from, size);
    }

    @Test
    void getItemsByUserId_shouldReturnItemsByUserId() throws Exception {
        Long userId = 1L;
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        Integer from = 0;
        Integer size = 20;

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

        when(itemService.getItemsByUserId(userId, from, size)).thenReturn(expectedItem);
        when(itemMapper.itemWithBookingsAndCommentsToDtos(expectedItem)).thenReturn(expectedItemDto);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemsByUserId(userId, from, size);
        verify(itemMapper, times(1)).itemWithBookingsAndCommentsToDtos(expectedItem);
    }

    @Test
    void getItemsByUserId_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 20;

        when(itemService.getItemsByUserId(userId, from, size)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemsByUserId(userId, from, size);
    }

    @Test
    void getItemById_shouldReturnItemById_ifTheUserIsTheOwner() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        User user = initUser();
        user.setId(userId);
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        item.setOwner(user);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.getItemById(itemId)).thenReturn(item);
        when(itemMapper.itemWithBookingsAndCommentsToDto(item)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", itemId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemById(itemId);
        verify(itemMapper, times(1)).itemWithBookingsAndCommentsToDto(item);
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
        when(itemMapper.itemWithCommentsToDto(item)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", itemId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemById(itemId);
        verify(itemMapper, times(1)).itemWithCommentsToDto(item);
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

        when(itemMapper.toItem(itemDto, userId)).thenReturn(item);
        when(itemService.createItem(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isCreated());

        verify(itemMapper, times(1)).toItem(itemDto, userId);
        verify(itemService, times(1)).createItem(item);
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    void createItem_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto, userId)).thenReturn(item);
        when(itemService.createItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemMapper, times(1)).toItem(itemDto, userId);
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

        when(itemMapper.toItem(itemDto, userId)).thenReturn(item);
        when(itemService.updateItem(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isOk());

        verify(itemMapper, times(1)).toItem(itemDto, userId);
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

        when(itemMapper.toItem(itemDto, userId)).thenReturn(item);
        when(itemService.updateItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemMapper, times(1)).toItem(itemDto, userId);
        verify(itemService, times(1)).updateItem(item);
        verify(itemMapper, never()).toDto(item);
    }

    @Test
    void updateItemById_shouldResponseWithNotFound_ifItemDoesNotExist() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;

        ItemDto itemDto = initItemDto();
        Item item = initItem();
        itemDto.setId(itemId);
        item.setId(itemId);

        String json = objectMapper.writeValueAsString(itemDto);

        when(itemMapper.toItem(itemDto, userId)).thenReturn(item);
        when(itemService.updateItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemMapper, times(1)).toItem(itemDto, userId);
        verify(itemService, times(1)).updateItem(item);
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
    void searchItems_shouldReturnEmptyListOfItems() throws Exception {
        String text = "аккумулятор";
        Integer from = 0;
        Integer size = 20;

        List<Item> expectedItem = List.of();
        List<ItemDto> expectedItemDto = List.of();
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text, from, size)).thenReturn(expectedItem);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text, from, size);
    }

    @Test
    void searchItems_shouldReturnItems() throws Exception {
        String text = "дрель";
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        Integer from = 0;
        Integer size = 20;

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

        when(itemService.searchItems(text, from, size)).thenReturn(expectedItem);
        when(itemMapper.toDtos(expectedItem)).thenReturn(expectedItemDto);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text, from, size);
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

        when(commentMapper.toComment(commentForCreateDto, itemId, userId)).thenReturn(comment);
        when(itemService.createComment(comment)).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentForResponseDto);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isOk());

        verify(commentMapper, times(1)).toComment(commentForCreateDto, itemId, userId);
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

        when(commentMapper.toComment(commentForCreateDto, itemId, userId)).thenReturn(comment);
        when(itemService.createComment(comment)).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(commentMapper, times(1)).toComment(commentForCreateDto, itemId, userId);
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

        when(commentMapper.toComment(commentForCreateDto, itemId, userId)).thenReturn(comment);
        when(itemService.createComment(comment)).thenThrow(ValidationException.class);

        mockMvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());

        verify(commentMapper, times(1)).toComment(commentForCreateDto, itemId, userId);
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
                Arguments.of(initItemDto(itemDto -> itemDto.setName(""))),
                Arguments.of(initItemDto(itemDto -> itemDto.setName("Д"))),
                Arguments.of(initItemDto(itemDto -> itemDto.setName("Дрель".repeat(20) + "ь"))),
                Arguments.of(initItemDto(itemDto -> itemDto.setDescription(""))),
                Arguments.of(initItemDto(itemDto -> itemDto.setDescription("Д"))),
                Arguments.of(initItemDto(itemDto -> itemDto.setDescription("Дрель".repeat(40) + "ь")))
        );
    }

    private static Stream<Arguments> provideInvalidComments() {
        return Stream.of(
                Arguments.of(initCommentForCreateDto(commentDto -> commentDto.setText(null))),
                Arguments.of(initCommentForCreateDto(commentDto -> commentDto.setText(""))),
                Arguments.of(initCommentForCreateDto(commentDto -> commentDto.setText("     "))),
                Arguments.of(initCommentForCreateDto(commentDto -> commentDto.setText("К"))),
                Arguments.of(initCommentForCreateDto(commentDto -> commentDto.setText("Комме".repeat(200) + "н")))
        );
    }

    private static ItemDto initItemDto(Consumer<ItemDto> consumer) {
        ItemDto itemDto = initItemDto();
        consumer.accept(itemDto);
        return itemDto;
    }

    private static CommentForCreateDto initCommentForCreateDto(Consumer<CommentForCreateDto> consumer) {
        CommentForCreateDto commentForCreateDto = initCommentForCreateDto();
        consumer.accept(commentForCreateDto);
        return commentForCreateDto;
    }

    private static User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }

    private static ItemDto initItemDto() {
        ItemDto itemDto = new ItemDto();

        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        return itemDto;
    }

    private static Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        return item;
    }

    private static CommentForCreateDto initCommentForCreateDto() {
        CommentForCreateDto commentDto = new CommentForCreateDto();
        commentDto.setText("Комментарий пользователя");
        return commentDto;
    }

    private static CommentForResponseDto initCommentForResponseDto() {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setText("Комментарий пользователя");
        commentDto.setAuthorName("Автор");
        commentDto.setCreated(LocalDateTime.now());

        return commentDto;
    }

    private static Comment initComment() {
        Comment comment = new Comment();
        comment.setText("Комментарий пользователя");
        return comment;
    }
}
