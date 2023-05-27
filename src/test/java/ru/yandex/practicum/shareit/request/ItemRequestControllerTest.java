package ru.yandex.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.yandex.practicum.shareit.user.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private ItemRequestService itemRequestService;

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestController itemRequestController;

    @BeforeEach
    void setMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemRequestController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void getItemRequestsAll_shouldReturnEmptyListOfRequests() throws Exception {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("created").descending());

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService, times(1)).getItemRequestsAll(userId, page);
    }

    @Test
    void getItemRequestsAll_shouldReturnAListOfRequests() throws Exception {
        Long userId = 1L;
        Long itemRequestId1 = 1L;
        Long itemRequestId2 = 2L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("created").descending());

        ItemRequestDto itemRequestDto1 = initItemRequestDto();
        ItemRequestDto itemRequestDto2 = initItemRequestDto();
        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();

        itemRequestDto1.setId(itemRequestId1);
        itemRequest1.setId(itemRequestId1);
        itemRequestDto2.setId(itemRequestId2);
        itemRequest2.setId(itemRequestId2);

        List<ItemRequest> expectedItemRequest = List.of(itemRequest1, itemRequest2);
        List<ItemRequestDto> expectedItemRequestDto = List.of(itemRequestDto1, itemRequestDto2);

        String json = objectMapper.writeValueAsString(expectedItemRequestDto);

        when(itemRequestService.getItemRequestsAll(userId, page)).thenReturn(expectedItemRequest);
        when(itemRequestMapper.toDtos(expectedItemRequest)).thenReturn(expectedItemRequestDto);

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemRequestService, times(1)).getItemRequestsAll(userId, page);
        verify(itemRequestService, times(1)).setItemsToItemRequests(expectedItemRequest);
        verify(itemRequestMapper, times(1)).toDtos(expectedItemRequest);

    }

    @Test
    void getItemRequestsAll_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("created").descending());

        when(itemRequestService.getItemRequestsAll(userId, page)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getItemRequestsAll(userId, page);
    }

    @Test
    void getItemRequestsByUserId_shouldReturnEmptyListOfRequests() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService, times(1)).getItemRequestsByUserId(userId);
    }

    @Test
    void getItemRequestsByUserId_shouldReturnAListOfRequests() throws Exception {
        Long userId = 1L;
        Long itemRequestId1 = 1L;
        Long itemRequestId2 = 2L;

        ItemRequestDto itemRequestDto1 = initItemRequestDto();
        ItemRequestDto itemRequestDto2 = initItemRequestDto();
        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();

        itemRequestDto1.setId(itemRequestId1);
        itemRequest1.setId(itemRequestId1);
        itemRequestDto2.setId(itemRequestId2);
        itemRequest2.setId(itemRequestId2);

        List<ItemRequest> expectedItemRequest = List.of(itemRequest1, itemRequest2);
        List<ItemRequestDto> expectedItemRequestDto = List.of(itemRequestDto1, itemRequestDto2);

        String json = objectMapper.writeValueAsString(expectedItemRequestDto);

        when(itemRequestService.getItemRequestsByUserId(userId)).thenReturn(expectedItemRequest);
        when(itemRequestMapper.toDtos(expectedItemRequest)).thenReturn(expectedItemRequestDto);

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemRequestService, times(1)).getItemRequestsByUserId(userId);
        verify(itemRequestService, times(1)).setItemsToItemRequests(expectedItemRequest);
    }

    @Test
    void getItemRequestsByUserId_shouldResponseWithNotFound_ifUserDoesNotExist() throws Exception {
        Long userId = 1L;

        when(itemRequestService.getItemRequestsByUserId(userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getItemRequestsByUserId(userId);
    }

    @Test
    void getItemRequestById_shouldReturnItemRequestById() throws Exception {
        Long userId = 1L;
        Long itemRequestId = 2L;

        ItemRequestDto itemRequestDto = initItemRequestDto();
        ItemRequest itemRequest = initItemRequest();

        String json = objectMapper.writeValueAsString(itemRequestDto);

        when(itemRequestService.getItemRequestById(itemRequestId, userId)).thenReturn(itemRequest);
        when(itemRequestMapper.toDto(itemRequest)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{id}", itemRequestId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(itemRequestService, times(1)).getItemRequestById(itemRequestId, userId);
        verify(itemRequestService, times(1)).setItemsToItemRequest(itemRequest);
        verify(itemRequestMapper, times(1)).toDto(itemRequest);

    }

    @Test
    void getItemRequestById_shouldResponseWithNotFound_ifItemRequestDoesNotExist() throws Exception {
        Long userId = 1L;
        Long itemRequestId = 2L;

        when(itemRequestService.getItemRequestById(itemRequestId, userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests/{id}", itemRequestId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getItemRequestById(itemRequestId, userId);
    }

    @Test
    void createRequest_shouldResponseWithOk() throws Exception {
        Long userId = 1L;

        ItemRequestDto itemRequestDto = initItemRequestDto();
        ItemRequest itemRequest = initItemRequest();

        String json = objectMapper.writeValueAsString(itemRequestDto);

        when(itemRequestMapper.toItemRequest(itemRequestDto)).thenReturn(itemRequest);
        when(itemRequestService.createRequest(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.toDto(itemRequest)).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isCreated());

        verify(itemRequestMapper, times(1)).toItemRequest(itemRequestDto);
        verify(itemRequestService, times(1)).createRequest(itemRequest);
        verify(itemRequestMapper, times(1)).toDto(itemRequest);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItemRequests")
    void createRequest_shouldResponseWithBadRequest_ifTheRequestIsInvalid(ItemRequestDto requestDto) throws Exception {
        Long userId = 1L;

        String json = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", userId)
                        .contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideInvalidItemRequests() {
        return Stream.of(
                Arguments.of(initItemRequestDto(dto -> dto.setDescription(""))),
                Arguments.of(initItemRequestDto(dto -> dto.setDescription("Д"))),
                Arguments.of(initItemRequestDto(dto -> dto.setDescription("Дрель".repeat(40) + "ь")))
        );
    }

    private static ItemRequestDto initItemRequestDto(Consumer<ItemRequestDto> consumer) {
        ItemRequestDto itemRequestDto = initItemRequestDto();
        consumer.accept(itemRequestDto);
        return itemRequestDto;
    }

    private static ItemRequestDto initItemRequestDto() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Хотел бы воспользоваться щеткой для обуви");
        return itemRequestDto;
    }

    private ItemRequest initItemRequest() {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setDescription("Хотел бы воспользоваться щеткой для обуви");
        itemRequest.setRequestor(new User());

        return itemRequest;
    }
}
