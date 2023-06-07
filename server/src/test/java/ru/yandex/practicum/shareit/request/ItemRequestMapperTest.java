package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemDto;
import ru.yandex.practicum.shareit.item.ItemMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {

    private final LocalDateTime currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toDto_shouldReturnItemRequestDto() {
        ItemRequest itemRequest = initItemRequest();

        Item item1 = initItem();
        Item item2 = initItem();
        ItemDto itemDto1 = initItemDto();
        ItemDto itemDto2 = initItemDto();

        List<Item> items = List.of(item1, item2);
        List<ItemDto> itemDtos = List.of(itemDto1, itemDto2);

        itemRequest.setItems(items);

        when(itemMapper.toDtos(items)).thenReturn(itemDtos);

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertThat(itemRequestDto.getId()).isEqualTo(1L);
        assertThat(itemRequestDto.getDescription()).isEqualTo("Хотел бы воспользоваться щеткой для обуви");
        assertThat(itemRequestDto.getCreated()).isEqualTo(currentDateTime);
        assertThat(itemRequestDto.getItems()).isEqualTo(itemDtos);
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
    void toItemRequest_shouldReturnItemRequest() {
        ItemRequestDto itemRequestDto = initItemRequestDto();

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);

        assertThat(itemRequest.getId()).isEqualTo(1L);
        assertThat(itemRequest.getDescription()).isEqualTo("Хотел бы воспользоваться щеткой для обуви");
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
        itemRequest.setCreated(currentDateTime);

        return itemRequest;
    }

    private ItemDto initItemDto() {
        ItemDto itemDto = new ItemDto();

        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        return itemDto;
    }

    private Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        return item;
    }
}
