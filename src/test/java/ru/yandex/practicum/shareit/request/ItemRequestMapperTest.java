package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemDto;
import ru.yandex.practicum.shareit.item.ItemMapper;
import ru.yandex.practicum.shareit.item.ItemService;
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
class ItemRequestMapperTest {

    private LocalDateTime currentDateTime;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestMapper itemRequestMapper;

    @BeforeEach
    void setUp() {
        currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);
    }

    @Test
    void toDto_shouldReturnItemRequestDto() {
        ItemRequest itemRequest = initItemRequest();

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertThat(itemRequestDto.getId()).isEqualTo(1L);
        assertThat(itemRequestDto.getDescription()).isEqualTo("Хотел бы воспользоваться щеткой для обуви");
        assertThat(itemRequestDto.getCreated()).isEqualTo(currentDateTime);
    }

    @Test
    void toDtos_shouldReturnEmptyListOfItemRequestDtos() {
        assertThat(itemRequestMapper.toDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void toDtos_shouldReturnListOfItemRequestDtos() {
        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();
        ItemRequestDto itemRequestDto1 = initItemRequestDto();
        ItemRequestDto itemRequestDto2 = initItemRequestDto();

        List<ItemRequestDto> expected = List.of(itemRequestDto1, itemRequestDto2);

        assertThat(itemRequestMapper.toDtos(List.of(itemRequest1, itemRequest2))).isEqualTo(expected);
    }

    @Test
    void itemRequestWithItemsToDto_shouldReturnItemRequestDto() {
        ItemRequest itemRequest = initItemRequest();

        Item item1 = initItem();
        Item item2 = initItem();
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();

        List<Item> items = List.of(item1, item2);
        List<ItemDto> itemDtos = List.of(itemDto1, itemDto2);

        when(itemService.getItemsByRequestId(1L)).thenReturn(items);
        when(itemMapper.toDtos(items)).thenReturn(itemDtos);

        ItemRequestDto itemRequestDto = itemRequestMapper.itemRequestWithItemsToDto(itemRequest);

        assertThat(itemRequestDto.getItems()).isEqualTo(itemDtos);

        verify(itemService, times(1)).getItemsByRequestId(1L);
        verify(itemMapper, times(1)).toDtos(items);

        items = null;
        when(itemService.getItemsByRequestId(1L)).thenReturn(items);
        itemRequestDto = itemRequestMapper.itemRequestWithItemsToDto(itemRequest);

        assertThat(itemRequestDto.getItems()).isEqualTo(Collections.emptyList());
        verify(itemMapper, never()).toDtos(items);
    }

    @Test
    void itemRequestWithItemsToDtos_shouldReturnListOfItemRequestDto() {
        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();
        itemRequest2.setId(2L);

        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2);

        Item item1 = initItem();
        Item item2 = initItem();
        Item item3 = initItem();
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();
        ItemDto itemDto3 = initItemDto();

        List<Long> itemRequestIds = List.of(1L, 2L);
        List<Item> items1 = List.of(item1, item2);
        List<Item> items2 = List.of(item3);
        List<ItemDto> itemDtos1 = List.of(itemDto1, itemDto2);
        List<ItemDto> itemDtos2 = List.of(itemDto3);

        Map<Long, List<Item>> mapItems = Map.of(1L, items1, 2L, items2);

        when(itemService.getItemsByRequestIds(itemRequestIds)).thenReturn(mapItems);
        when(itemMapper.toDtos(items1)).thenReturn(itemDtos1);
        when(itemMapper.toDtos(items2)).thenReturn(itemDtos2);

        List<ItemRequestDto> itemRequestDtos = itemRequestMapper.itemRequestWithItemsToDtos(itemRequests);

        assertThat(itemRequestDtos.get(0).getItems()).isEqualTo(itemDtos1);
        assertThat(itemRequestDtos.get(1).getItems()).isEqualTo(itemDtos2);
    }

    @Test
    void itemRequestWithItemsToDtos_shouldReturnEmptyListOfItemRequestDto() {
        assertThat(itemRequestMapper.itemRequestWithItemsToDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void toItemRequest_shouldReturnItemRequest() {
        Long authorId = 2L;
        ItemRequestDto itemRequestDto = initItemRequestDto();

        User author = initUser();
        author.setId(authorId);

        when(userService.getUserById(authorId)).thenReturn(author);

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, authorId);

        assertThat(itemRequest.getId()).isEqualTo(1L);
        assertThat(itemRequest.getDescription()).isEqualTo("Хотел бы воспользоваться щеткой для обуви");
        assertThat(itemRequest.getRequester()).isEqualTo(author);

        verify(userService, times(1)).getUserById(authorId);
    }

    private ItemRequestDto initItemRequestDto() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();

        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Хотел бы воспользоваться щеткой для обуви");
        itemRequestDto.setCreated(currentDateTime);

        return itemRequestDto;
    }

    private ItemRequest initItemRequest() {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setId(1L);
        itemRequest.setDescription("Хотел бы воспользоваться щеткой для обуви");
        itemRequest.setRequester(new User());
        itemRequest.setCreated(currentDateTime);

        return itemRequest;
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

    private User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}
