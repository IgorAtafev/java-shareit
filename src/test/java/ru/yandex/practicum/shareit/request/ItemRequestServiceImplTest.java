package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.Collections;
import java.util.List;
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

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getItemRequestsAll_shouldReturnEmptyListOfRequests() {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 20;
        PageRequest page = PageRequest.of(0, size, Sort.by("created").descending());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequesterIdNot(userId, page))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        assertThat(itemRequestService.getItemRequestsAll(userId, from, size)).isEmpty();

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequesterIdNot(userId, page);
    }

    @Test
    void getItemRequestsAll_shouldReturnAListOfRequests() {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 20;
        PageRequest page = PageRequest.of(0, size, Sort.by("created").descending());

        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();

        List<ItemRequest> expected = List.of(itemRequest1, itemRequest2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequesterIdNot(userId, page)).thenReturn(new PageImpl<>(expected));

        assertThat(itemRequestService.getItemRequestsAll(userId, from, size)).isEqualTo(expected);

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequesterIdNot(userId, page);
    }

    @Test
    void getItemRequestsAll_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 20;
        PageRequest page = PageRequest.of(0, size, Sort.by("created").descending());

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestsAll(userId, from, size));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, never()).findByRequesterIdNot(userId, page);
    }

    @Test
    void getItemRequestsByUserId_shouldReturnEmptyListOfRequests() {
        Long userId = 1L;
        Sort sort = Sort.by("created").descending();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequesterId(userId, sort)).thenReturn(Collections.emptyList());

        assertThat(itemRequestService.getItemRequestsByUserId(userId)).isEmpty();

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequesterId(userId, sort);
    }

    @Test
    void getItemRequestsByUserId_shouldReturnAListOfRequests() {
        Long userId = 1L;
        Sort sort = Sort.by("created").descending();

        ItemRequest itemRequest1 = initItemRequest();
        ItemRequest itemRequest2 = initItemRequest();

        List<ItemRequest> expected = List.of(itemRequest1, itemRequest2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequesterId(userId, sort)).thenReturn(expected);

        assertThat(itemRequestService.getItemRequestsByUserId(userId)).isEqualTo(expected);

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, times(1)).findByRequesterId(userId, sort);
    }

    @Test
    void getItemRequestsByUserId_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Sort sort = Sort.by("created").descending();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestsByUserId(userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(itemRequestRepository, never()).findByRequesterId(userId, sort);
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

    private ItemRequest initItemRequest() {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setDescription("Хотел бы воспользоваться щеткой для обуви");
        itemRequest.setRequester(new User());

        return itemRequest;
    }
}
