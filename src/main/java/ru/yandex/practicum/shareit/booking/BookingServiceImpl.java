package ru.yandex.practicum.shareit.booking;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;
import ru.yandex.practicum.shareit.validator.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public Iterable<Booking> getBookingsByUserId(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        BookingListState bookingListState = getBookingListState(state);
        BooleanExpression expression = QBooking.booking.booker.id.eq(userId);
        Predicate predicate = getPredicateByUserIdAndState(userId, bookingListState, expression);

        return bookingRepository.findAll(predicate, Sort.by("start").descending());
    }

    @Override
    public Iterable<Booking> getBookingsByItemOwnerId(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        BookingListState bookingListState = getBookingListState(state);
        BooleanExpression expression = QBooking.booking.item.owner.id.eq(userId);
        Predicate predicate = getPredicateByUserIdAndState(userId, bookingListState, expression);

        return bookingRepository.findAll(predicate, Sort.by("start").descending());
    }

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

    @Override
    public Booking createBooking(Booking booking) {
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new NotFoundException("The owner of the item and the booker are the same");
        }

        if (booking.getStart().isAfter(booking.getEnd()) || booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("The start of the booking must be before the end of the booking");
        }

        if (Objects.equals(Boolean.FALSE, booking.getItem().getAvailable())) {
            throw new ValidationException(
                    String.format("Item with id %d not available for booking", booking.getItem().getId())
            );
        }

        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

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

    @Override
    public Map<Long, List<Booking>> getBookingsByItemIds(List<Long> itemIds) {
        return bookingRepository.findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED,
                        Sort.by("start").ascending()).stream()
                        .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
    }

    private BookingListState getBookingListState(String state) {
        try {
            return BookingListState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private Predicate getPredicateByUserIdAndState(
            Long userId, BookingListState bookingListState, BooleanExpression sourceExpression
    ) {
        LocalDateTime now = LocalDateTime.now();
        BooleanExpression addExpression = null;
        QBooking booking = QBooking.booking;

        switch (bookingListState) {
            case CURRENT:
                addExpression = booking.start.lt(now).and(booking.end.gt(now));
                break;
            case PAST:
                addExpression = booking.end.lt(now);
                break;
            case FUTURE:
                addExpression = booking.start.gt(now);
                break;
            case WAITING:
                addExpression = booking.status.eq(BookingStatus.WAITING);
                break;
            case REJECTED:
                addExpression = booking.status.eq(BookingStatus.REJECTED);
        }

        if (addExpression != null) {
            return sourceExpression.and(addExpression);
        }
        return sourceExpression;
    }
}
