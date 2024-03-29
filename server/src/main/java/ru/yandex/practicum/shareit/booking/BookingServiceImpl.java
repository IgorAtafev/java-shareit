package ru.yandex.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByUserId(Long userId, String state, Pageable page) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        BookingListState bookingListState = getBookingListState(state);
        Specification<Booking> specification = BookingSpecification.byBookerId(userId);
        if (bookingListState.getSpecification() != null) {
            specification = Specification.where(specification).and(bookingListState.getSpecification());
        }

        return bookingRepository.findAll(specification, page).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByItemOwnerId(Long userId, String state, Pageable page) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        BookingListState bookingListState = getBookingListState(state);
        Specification<Booking> specification = BookingSpecification.byItemOwnerId(userId);
        if (bookingListState.getSpecification() != null) {
            specification = Specification.where(specification).and(bookingListState.getSpecification());
        }

        return bookingRepository.findAll(specification, page).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Booking getBookingById(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Booking with id %d does not exist", id)));

        if (!Objects.equals(userId, booking.getBooker().getId())
                && !Objects.equals(userId, booking.getItem().getOwner().getId())
        ) {
            throw new NotFoundException(String.format("Booking with id %d and user id %d does not exist", id, userId));
        }

        return booking;
    }

    @Transactional
    @Override
    public Booking createBooking(Booking booking) {
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new NotFoundException("The owner of the item and the booker are the same");
        }

        if (Objects.equals(Boolean.FALSE, booking.getItem().getAvailable())) {
            throw new ValidationException(
                    String.format("Item with id %d not available for booking", booking.getItem().getId())
            );
        }

        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Transactional
    @Override
    public Booking approveBookingById(Long id, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Booking with id %d does not exist", id)));

        if (!Objects.equals(userId, booking.getItem().getOwner().getId())) {
            throw new NotFoundException(String.format("Booking with id %d and owner id %d does not exist", id, userId));
        }

        if (!Objects.equals(BookingStatus.WAITING, booking.getStatus())) {
            throw new ValidationException(
                    String.format("Booking with id %d not in status WAITING", booking.getId())
            );
        }

        if (Objects.equals(Boolean.TRUE, approved)) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, List<Booking>> getBookingsByItemIds(List<Long> itemIds) {
        return bookingRepository.findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED,
                        Sort.by("start").ascending()).stream()
                        .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByItemId(Long itemId) {
        return new ArrayList<>(bookingRepository.findByItemIdAndStatus(itemId, BookingStatus.APPROVED,
                Sort.by("start").ascending()));
    }

    @Override
    public Booking getLastBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .reduce((booking1, booking2) -> booking2)
                .orElse(null);
    }

    @Override
    public Booking getNextBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);
    }

    private BookingListState getBookingListState(String state) {
        try {
            return BookingListState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
