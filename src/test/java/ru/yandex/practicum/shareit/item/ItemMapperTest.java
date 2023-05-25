package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.shareit.request.ItemRequest;
import ru.yandex.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapper();

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
}
