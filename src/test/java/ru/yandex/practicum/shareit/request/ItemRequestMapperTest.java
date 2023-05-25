package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    private final ItemRequestMapper itemRequestMapper = new ItemRequestMapper();
    private LocalDateTime currentDateTime;

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
        itemRequest.setRequestor(new User());
        itemRequest.setCreated(currentDateTime);

        return itemRequest;
    }
}
