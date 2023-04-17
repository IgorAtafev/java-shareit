package ru.yandex.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.shareit.user.UserService;
import ru.yandex.practicum.shareit.validator.ErrorHandler;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1)).getItemsByUserId(userId);
    }

    @Test
    void getItemsByUserId_shouldReturnItemsByUserId() throws Exception {
        Long userId = 1L;
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> expectedItem = List.of(item1, item2);
        List<ItemDto> expectedItemDto = List.of(itemDto1, itemDto2);
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.getItemsByUserId(userId)).thenReturn(expectedItem);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemsByUserId(userId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void getItemsByUserId_shouldResponseWithNotFound_ifUserDoesNotExist(Long userId) throws Exception {
        when(itemService.getItemsByUserId(userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemsByUserId(userId);
    }

    @Test
    void getItemById_shouldReturnItemById() throws Exception {
        Long itemId = 1L;
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.getItemById(itemId)).thenReturn(item);

        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).getItemById(itemId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void getItemById_shouldResponseWithNotFound_ifItemDoesNotExist(Long itemId) throws Exception {
        when(itemService.getItemById(itemId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemById(itemId);
    }

    @Test
    void createItem_shouldResponseWithOk() throws Exception {
        Long userId = 1L;
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.createItem(item)).thenReturn(item);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isCreated());

        verify(itemService, times(1)).createItem(item);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void createItem_shouldResponseWithNotFound_ifUserDoesNotExist(Long userId) throws Exception {
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.createItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).createItem(item);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItems")
    void createItem_shouldResponseWithBadRequest_ifItemIsInvalid(ItemDto itemDto) throws Exception {
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

        when(itemService.updateItem(item)).thenReturn(item);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isOk());

        verify(itemService, times(1)).updateItem(item);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void updateItemById_shouldResponseWithNotFound_ifUserDoesNotExist(Long userId) throws Exception {
        Long itemId = 2L;
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        itemDto.setId(itemId);
        item.setId(itemId);
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.updateItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).updateItem(item);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void updateItemById_shouldResponseWithNotFound_ifItemDoesNotExist(Long itemId) throws Exception {
        Long userId = 1L;
        ItemDto itemDto = initItemDto();
        Item item = initItem();
        itemDto.setId(itemId);
        item.setId(itemId);
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.updateItem(item)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/{id}", itemId).header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).updateItem(item);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItems")
    void updateItemById_shouldResponseWithBadRequest_ifItemIsInvalid(ItemDto itemDto) throws Exception {
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

        List<Item> expectedItem = List.of();
        List<ItemDto> expectedItemDto = List.of();
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text)).thenReturn(expectedItem);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentInTheNameAndDescription() throws Exception {
        String text = "дрель";
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> expectedItem = List.of(item1, item2);
        List<ItemDto> expectedItemDto = List.of(itemDto1, itemDto2);
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text)).thenReturn(expectedItem);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentOnlyInTheName() throws Exception {
        String text = "дрель";
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        Item item1 = initItem();
        Item item2 = initItem();

        itemDto1.setName("Аккумулятор");
        itemDto1.setDescription("Аккумулятор");
        itemDto2.setDescription("Простая");
        item1.setName("Аккумулятор");
        item1.setDescription("Аккумулятор");
        item2.setDescription("Простая");

        List<Item> expectedItem = List.of(item2);
        List<ItemDto> expectedItemDto = List.of(itemDto2);
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text)).thenReturn(expectedItem);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentOnlyInTheDescription() throws Exception {
        String text = "прост";
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        Item item1 = initItem();
        Item item2 = initItem();

        itemDto2.setName("Аккумулятор");
        itemDto2.setDescription("Аккумулятор");
        item2.setName("Аккумулятор");
        item2.setDescription("Аккумулятор");

        List<Item> expectedItem = List.of(item1);
        List<ItemDto> expectedItemDto = List.of(itemDto1);
        String json = objectMapper.writeValueAsString(expectedItemDto);

        when(itemService.searchItems(text)).thenReturn(expectedItem);

        mockMvc.perform(get("/items/search?text={text}", text))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemService, times(1)).searchItems(text);
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

    private static ItemDto initItemDto(Consumer<ItemDto> consumer) {
        ItemDto itemDto = initItemDto();

        consumer.accept(itemDto);

        return itemDto;
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
}