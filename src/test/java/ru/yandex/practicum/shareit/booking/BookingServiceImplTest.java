package ru.yandex.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

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
    void createBooking_shouldCreateABooking_ifTheBookerAndTheOwnerOfTheItemAreTheSame() {
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
    void createBooking_shouldCreateABooking_ifTheBookingStartDateIsGreaterThanTheEndDate() {
        Long userId = 1L;
        Long itemId = 2L;

        Booking booking = initBooking();
        booking.setStart(LocalDateTime.now().plusHours(2));
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.getItem().setId(itemId);
        booking.getBooker().setId(userId);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.createBooking(booking));

        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_shouldCreateABooking_ifTheBookingStartDateIsTheSameAsTheEndDate() {
        Long userId = 1L;
        Long itemId = 2L;

        Booking booking = initBooking();
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.getItem().setId(itemId);
        booking.getBooker().setId(userId);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> bookingService.createBooking(booking));

        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_shouldCreateABooking_ifTheItemIsNotAvailable() {
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

        when(bookingRepository.findById(bookingId)).thenThrow(NotFoundException.class);

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

    private static Booking initBooking() {
        Booking booking = new Booking();

        LocalDateTime currentDateTime = LocalDateTime.now();
        booking.setStart(currentDateTime.plusHours(1));
        booking.setEnd(currentDateTime.plusHours(2));
        booking.setItem(new Item());
        booking.getItem().setOwner(new User());
        booking.setBooker(new User());
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }
}
