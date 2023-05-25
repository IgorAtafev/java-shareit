package ru.yandex.practicum.shareit.booking;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private LocalDateTime currentDateTime;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);
    }

    @Test
    void getBookingsByUserId_shouldReturnAListOfUserBookings() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        Booking booking3 = initBooking();
        Booking booking4 = initBooking();

        booking1.getBooker().setId(userId);
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setStart(currentDateTime.plusHours(1));
        booking1.setEnd(currentDateTime.plusHours(2));

        booking2.getBooker().setId(userId);
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setStart(currentDateTime.minusHours(2));
        booking2.setEnd(currentDateTime.minusHours(1));

        booking3.getBooker().setId(userId);
        booking3.setStatus(BookingStatus.REJECTED);
        booking3.setStart(currentDateTime.minusHours(2));
        booking3.setEnd(currentDateTime.plusHours(1));

        booking4.getBooker().setId(userId);
        booking4.setStatus(BookingStatus.REJECTED);
        booking4.setStart(currentDateTime.minusHours(1));
        booking4.setEnd(currentDateTime.plusHours(2));

        when(userRepository.existsById(userId)).thenReturn(true);

        QBooking qBooking = QBooking.booking;

        String state = "ALL";
        BooleanExpression sourceExpression = qBooking.booker.id.eq(userId);
        Predicate predicate = sourceExpression;
        List<Booking> expected = List.of(booking1, booking2, booking3, booking4);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        assertThat(bookingService.getBookingsByUserId(userId, state, page)).isEqualTo(expected);
        verify(userRepository, times(1)).existsById(userId);
        verify(bookingRepository, times(1)).findAll(predicate, page);

        state = "CURRENT";
        BooleanExpression addExpression = qBooking.start.lt(currentDateTime).and(qBooking.end.gt(currentDateTime));
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking3, booking4);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getBookingsByUserId(userId, state, page)).isEqualTo(expected);
        }

        state = "PAST";
        addExpression = qBooking.end.lt(currentDateTime);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking2);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getBookingsByUserId(userId, state, page)).isEqualTo(expected);
        }

        state = "FUTURE";
        addExpression = qBooking.start.gt(currentDateTime);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking1);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getBookingsByUserId(userId, state, page)).isEqualTo(expected);
        }

        state = "WAITING";
        addExpression = qBooking.status.eq(BookingStatus.WAITING);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking1, booking2);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        assertThat(bookingService.getBookingsByUserId(userId, state, page)).isEqualTo(expected);

        state = "REJECTED";
        addExpression = qBooking.status.eq(BookingStatus.REJECTED);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking3, booking4);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        assertThat(bookingService.getBookingsByUserId(userId, state, page)).isEqualTo(expected);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.getBookingsByUserId(userId, "UNDEFINED", page));
    }

    @Test
    void getBookingsByUserId_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Integer size = 20;
        String state = "ALL";
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.getBookingsByUserId(userId, state, page));

        verify(userRepository, times(1)).existsById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getBookingsByItemOwnerId_shouldReturnAListOfBookingsForAllTheUserItems() {
        Long userId = 1L;
        Integer size = 20;
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        Booking booking3 = initBooking();
        Booking booking4 = initBooking();

        booking1.getItem().getOwner().setId(userId);
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setStart(currentDateTime.plusHours(1));
        booking1.setEnd(currentDateTime.plusHours(2));

        booking2.getItem().getOwner().setId(userId);
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setStart(currentDateTime.minusHours(2));
        booking2.setEnd(currentDateTime.minusHours(1));

        booking3.getItem().getOwner().setId(userId);
        booking3.setStatus(BookingStatus.REJECTED);
        booking3.setStart(currentDateTime.minusHours(2));
        booking3.setEnd(currentDateTime.plusHours(1));

        booking4.getItem().getOwner().setId(userId);
        booking4.setStatus(BookingStatus.REJECTED);
        booking4.setStart(currentDateTime.minusHours(1));
        booking4.setEnd(currentDateTime.plusHours(2));

        when(userRepository.existsById(userId)).thenReturn(true);

        QBooking qBooking = QBooking.booking;

        String state = "ALL";
        BooleanExpression sourceExpression = qBooking.item.owner.id.eq(userId);
        Predicate predicate = sourceExpression;
        List<Booking> expected = List.of(booking1, booking2, booking3, booking4);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        assertThat(bookingService.getBookingsByItemOwnerId(userId, state, page)).isEqualTo(expected);
        verify(userRepository, times(1)).existsById(userId);
        verify(bookingRepository, times(1)).findAll(predicate, page);

        state = "CURRENT";
        BooleanExpression addExpression = qBooking.start.lt(currentDateTime).and(qBooking.end.gt(currentDateTime));
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking3, booking4);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getBookingsByItemOwnerId(userId, state, page)).isEqualTo(expected);
        }

        state = "PAST";
        addExpression = qBooking.end.lt(currentDateTime);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking2);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getBookingsByItemOwnerId(userId, state, page)).isEqualTo(expected);
        }

        state = "FUTURE";
        addExpression = qBooking.start.gt(currentDateTime);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking1);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getBookingsByItemOwnerId(userId, state, page)).isEqualTo(expected);
        }

        state = "WAITING";
        addExpression = qBooking.status.eq(BookingStatus.WAITING);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking1, booking2);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        assertThat(bookingService.getBookingsByItemOwnerId(userId, state, page)).isEqualTo(expected);

        state = "REJECTED";
        addExpression = qBooking.status.eq(BookingStatus.REJECTED);
        predicate = sourceExpression.and(addExpression);
        expected = List.of(booking3, booking4);

        when(bookingRepository.findAll(predicate, page)).thenReturn(new PageImpl<>(expected));
        assertThat(bookingService.getBookingsByItemOwnerId(userId, state, page)).isEqualTo(expected);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.getBookingsByItemOwnerId(userId, "UNDEFINED", page));
    }

    @Test
    void getBookingsByItemOwnerId_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;
        Integer size = 20;
        String state = "ALL";
        Pageable page = PageRequest.of(0, size, Sort.by("start").descending());

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.getBookingsByItemOwnerId(userId, state, page));

        verify(userRepository, times(1)).existsById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getBookingById_shouldReturnBookingById() {
        Long userId = 1L;
        Long bookingId = 2L;
        Booking booking = initBooking();
        booking.getBooker().setId(userId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThat(bookingService.getBookingById(bookingId, userId)).isEqualTo(booking);

        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void getBookingById_shouldThrowAnException_ifBookingDoesNotExist() {
        Long userId = 1L;
        Long bookingId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.getBookingById(bookingId, userId));

        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void getBookingById_shouldThrowAnException_ifTheUserBookingDoesNotExist() {
        Long userId = 1L;
        Long bookingId = 2L;
        Booking booking = initBooking();
        booking.getBooker().setId(2L);
        booking.getItem().getOwner().setId(3L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.getBookingById(bookingId, userId));

        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void createBooking_shouldCreateABooking() {
        Long userId = 1L;
        Long itemId = 2L;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getBooker().setId(userId);

        when(bookingRepository.save(booking)).thenReturn(booking);

        assertThat(bookingService.createBooking(booking)).isEqualTo(booking);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void createBooking_shouldThrowAnException_ifTheBookerAndTheOwnerOfTheItemAreTheSame() {
        Long userId = 1L;
        Long itemId = 2L;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getItem().getOwner().setId(userId);
        booking.getBooker().setId(userId);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.createBooking(booking));

        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_shouldThrowAnException_ifTheBookingStartDateIsGreaterThanTheEndDate() {
        Long userId = 1L;
        Long itemId = 2L;
        LocalDateTime currentDateTime = LocalDateTime.now();

        Booking booking = initBooking();
        booking.setStart(currentDateTime.plusHours(2));
        booking.setEnd(currentDateTime.plusHours(1));
        booking.getItem().setId(itemId);
        booking.getBooker().setId(userId);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.createBooking(booking));

        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_shouldThrowAnException_ifTheBookingStartDateIsTheSameAsTheEndDate() {
        Long userId = 1L;
        Long itemId = 2L;
        LocalDateTime currentDateTime = LocalDateTime.now();

        Booking booking = initBooking();
        booking.setStart(currentDateTime.plusHours(1));
        booking.setEnd(currentDateTime.plusHours(1));
        booking.getItem().setId(itemId);
        booking.getBooker().setId(userId);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.createBooking(booking));

        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_shouldThrowAnException_ifTheItemIsNotAvailable() {
        Long userId = 1L;
        Long itemId = 2L;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getBooker().setId(userId);
        booking.getItem().setAvailable(false);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.createBooking(booking));

        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveBookingById_shouldApproveTheBooking() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;
        Boolean approved = true;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getItem().getOwner().setId(userId);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        assertThat(bookingService.approveBookingById(bookingId, approved, userId)).isEqualTo(booking);

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void approveBookingById_shouldRejectTheBooking() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;
        Boolean approved = false;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getItem().getOwner().setId(userId);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        assertThat(bookingService.approveBookingById(bookingId, approved, userId)).isEqualTo(booking);

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void approveBookingById_shouldThrowAnException_ifBookingDoesNotExist() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;
        Boolean approved = true;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getItem().getOwner().setId(userId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.approveBookingById(bookingId, approved, userId));

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveBookingById_shouldThrowAnException_ifTheOwnerOfTheItemDoesNotExist() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;
        Boolean approved = true;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> bookingService.approveBookingById(bookingId, approved, userId));

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveBookingById_shouldThrowAnException_ifTheBookingStatusIsNotWaiting() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;
        Boolean approved = true;

        Booking booking = initBooking();
        booking.getItem().setId(itemId);
        booking.getItem().getOwner().setId(userId);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.approveBookingById(bookingId, approved, userId));

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void getBookingsByItemIds_shouldReturnEmptyListOfBookings() {
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        List<Long> itemIds = List.of(itemId1, itemId2);
        BookingStatus status = BookingStatus.APPROVED;
        Sort sort = Sort.by("start").ascending();

        when(bookingRepository.findByItemIdInAndStatus(itemIds, status, sort)).thenReturn(Collections.emptyList());

        assertThat(bookingService.getBookingsByItemIds(itemIds)).isEqualTo(Map.of());

        verify(bookingRepository, times(1)).findByItemIdInAndStatus(itemIds, status, sort);
    }

    @Test
    void getBookingsByItemIds_shouldReturnAListOfBookingsForItemIds() {
        Long itemId1 = 1L;
        Long itemId2 = 2L;
        List<Long> itemIds = List.of(itemId1, itemId2);
        BookingStatus status = BookingStatus.APPROVED;
        Sort sort = Sort.by("start").ascending();

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();
        Booking booking3 = initBooking();

        booking1.getItem().setId(itemId1);
        booking2.getItem().setId(itemId1);
        booking3.getItem().setId(itemId2);

        List<Booking> bookings = List.of(booking1, booking2, booking3);
        Map<Long, List<Booking>> expected = Map.of(itemId1, List.of(booking1, booking2), itemId2, List.of(booking3));

        when(bookingRepository.findByItemIdInAndStatus(itemIds, status, sort)).thenReturn(bookings);

        assertThat(bookingService.getBookingsByItemIds(itemIds)).isEqualTo(expected);

        verify(bookingRepository, times(1)).findByItemIdInAndStatus(itemIds, status, sort);
    }

    @Test
    void getBookingsByItemId_shouldReturnEmptyListOfBookings() {
        Long itemId = 1L;
        BookingStatus status = BookingStatus.APPROVED;
        Sort sort = Sort.by("start").ascending();

        when(bookingRepository.findByItemIdAndStatus(itemId, status, sort)).thenReturn(Collections.emptyList());

        assertThat(bookingService.getBookingsByItemId(itemId)).isEmpty();

        verify(bookingRepository, times(1)).findByItemIdAndStatus(itemId, status, sort);
    }

    @Test
    void getBookingsByItemId_shouldReturnAListOfBookingsForItemId() {
        Long itemId = 1L;
        BookingStatus status = BookingStatus.APPROVED;
        Sort sort = Sort.by("start").ascending();

        Booking booking1 = initBooking();
        Booking booking2 = initBooking();

        booking1.getItem().setId(itemId);
        booking2.getItem().setId(itemId);

        List<Booking> expected = List.of(booking1, booking2);

        when(bookingRepository.findByItemIdAndStatus(itemId, status, sort)).thenReturn(expected);

        assertThat(bookingService.getBookingsByItemId(itemId)).isEqualTo(expected);

        verify(bookingRepository, times(1)).findByItemIdAndStatus(itemId, status, sort);
    }

    @Test
    void getLastBooking_shouldReturnTheLastBookingBeforeTheCurrentTime() {
        Booking booking1 = initBooking();
        Booking booking2 = initBooking();

        booking1.setStart(currentDateTime.minusHours(2));
        booking1.setEnd(currentDateTime.minusHours(1));

        booking2.setStart(currentDateTime.plusHours(1));
        booking1.setEnd(currentDateTime.plusHours(2));

        assertThat(bookingService.getLastBooking(null)).isNull();
        assertThat(bookingService.getLastBooking(List.of())).isNull();

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getLastBooking(List.of(booking1, booking2))).isEqualTo(booking1);
        }

        booking1.setEnd(currentDateTime.plusHours(1));

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getLastBooking(List.of(booking1, booking2))).isEqualTo(booking1);
        }

        booking1.setStart(currentDateTime.plusHours(1));
        booking1.setEnd(currentDateTime.plusHours(2));

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getLastBooking(List.of(booking1, booking2))).isEqualTo(null);
        }
    }

    @Test
    void getNextBooking_shouldReturnTheFirstBookingAfterTheCurrentTime() {
        Booking booking1 = initBooking();
        Booking booking2 = initBooking();

        booking1.setStart(currentDateTime.minusHours(2));
        booking1.setEnd(currentDateTime.minusHours(1));

        booking2.setStart(currentDateTime.plusHours(1));
        booking2.setEnd(currentDateTime.plusHours(2));

        assertThat(bookingService.getNextBooking(null)).isNull();
        assertThat(bookingService.getNextBooking(List.of())).isNull();

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getNextBooking(List.of(booking1, booking2))).isEqualTo(booking2);
        }

        booking2.setStart(currentDateTime.minusHours(2));

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(currentDateTime);
            assertThat(bookingService.getNextBooking(List.of(booking1, booking2))).isEqualTo(null);
        }
    }

    private Booking initBooking() {
        Booking booking = new Booking();

        booking.setStart(currentDateTime.plusHours(1));
        booking.setEnd(currentDateTime.plusHours(2));
        booking.setItem(new Item());
        booking.getItem().setOwner(new User());
        booking.setBooker(new User());
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }
}
