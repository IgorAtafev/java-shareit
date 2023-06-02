package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.item.ItemService;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getItemRequestsAll_shouldReturnEmptyListOfRequests() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("created").descending());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdNot(userId, page)).thenReturn(Collections.emptyList());

        assertThat(itemRequestService.getItemRequestsAll(userId, page)).isEmpty();

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequestorIdNot(userId, page);
    }

    @Test
    void getItemRequestsAll_shouldReturnAListOfRequests() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("created").descending());

        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();

        List<ItemRequest> expected = List.of(itemRequest1, itemRequest2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdNot(userId, page)).thenReturn(expected);

        assertThat(itemRequestService.getItemRequestsAll(userId, page)).isEqualTo(expected);

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequestorIdNot(userId, page);
    }

    @Test
    void getItemRequestsAll_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("created").descending());

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestsAll(userId, page));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, never()).findByRequestorIdNot(userId, page);
    }

    @Test
    void getItemRequestsByUserId_shouldReturnEmptyListOfRequests() {
        Long userId = 1L;
        Sort sort = Sort.by("created").descending();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorId(userId, sort)).thenReturn(Collections.emptyList());

        assertThat(itemRequestService.getItemRequestsByUserId(userId)).isEmpty();

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequestorId(userId, sort);
    }

    @Test
    void getItemRequestsByUserId_shouldReturnAListOfRequests() {
        Long userId = 1L;
        Sort sort = Sort.by("created").descending();

        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();

        List<ItemRequest> expected = List.of(itemRequest1, itemRequest2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorId(userId, sort)).thenReturn(expected);

        assertThat(itemRequestService.getItemRequestsByUserId(userId)).isEqualTo(expected);

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequestorId(userId, sort);
    }

    @Test
    void getItemRequestsByUserId_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Sort sort = Sort.by("created").descending();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestsByUserId(userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, never()).findByRequestorId(userId, sort);
    }

    @Test
    void getItemRequestById_shouldReturnItemRequestById() {
        Long userId = 1L;
        Long itemRequestId = 2L;

        ItemRequest itemRequest = initItemRequest();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.of(itemRequest));

        assertThat(itemRequestService.getItemRequestById(itemRequestId, userId)).isEqualTo(itemRequest);

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findById(itemRequestId);
    }

    @Test
    void getItemRequestById_shouldThrowAnException_ifItemRequestDoesNotExist() {
        Long userId = 1L;
        Long itemRequestId = 2L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestById(itemRequestId, userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findById(itemRequestId);
    }

    @Test
    void getItemRequestById_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Long itemRequestId = 2L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestById(itemRequestId, userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, never()).findById(itemRequestId);
    }

    @Test
    void createRequest_shouldCreateARequest() {
        ItemRequest itemRequest = initItemRequest();

        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);

        assertThat(itemRequestService.createRequest(itemRequest)).isEqualTo(itemRequest);

        verify(itemRequestRepository, times(1)).save(itemRequest);
    }

    @Test
    void setItemsToItemRequests_shouldSetAListOfItemsToRequests() {
        Long itemRequestId1 = 1L;
        Long itemRequestId2 = 2L;

        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();
        itemRequest1.setId(itemRequestId1);
        itemRequest2.setId(itemRequestId2);

        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2);

        Item item1 = initItem();
        Item item2 = initItem();
        Item item3 = initItem();

        List<Long> itemRequestIds = List.of(itemRequestId1, itemRequestId2);
        List<Item> items1 = List.of(item1, item2);
        List<Item> items2 = List.of(item3);

        Map<Long, List<Item>> mapItems = Map.of(itemRequestId1, items1, itemRequestId2, items2);

        when(itemService.getItemsByRequestIds(itemRequestIds)).thenReturn(mapItems);

        itemRequestService.setItemsToItemRequests(itemRequests);

        assertThat(itemRequests.get(0).getItems()).isEqualTo(items1);
        assertThat(itemRequests.get(1).getItems()).isEqualTo(items2);
    }

    @Test
    void setItemsToItemRequest_shouldSetTheListOfItemsToTheRequest() {
        Long itemRequestId = 1L;

        ItemRequest itemRequest = initItemRequest();
        itemRequest.setId(itemRequestId);

        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> items = List.of(item1, item2);

        when(itemService.getItemsByRequestId(itemRequestId)).thenReturn(items);

        itemRequestService.setItemsToItemRequest(itemRequest);

        assertThat(itemRequest.getItems()).isEqualTo(items);

        verify(itemService, times(1)).getItemsByRequestId(itemRequestId);
    }

    private ItemRequest initItemRequest() {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setDescription("Хотел бы воспользоваться щеткой для обуви");
        itemRequest.setRequestor(new User());

        return itemRequest;
    }

    private Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setOwner(new User());

        return item;
    }
}
