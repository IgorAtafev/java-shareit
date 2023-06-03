package ru.yandex.practicum.shareit.booking;

import org.springframework.data.jpa.domain.Specification;

public enum BookingListState {

    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public Specification<Booking> getSpecification() {
        if (this == CURRENT) {
            return BookingSpecification.hasCurrent();
        }

        if (this == PAST) {
            return BookingSpecification.hasPast();
        }

        if (this == FUTURE) {
            return BookingSpecification.hasFuture();
        }

        if (this == WAITING) {
            return BookingSpecification.hasWaiting();
        }

        if (this == REJECTED) {
            return BookingSpecification.hasRejected();
        }

        return null;
    }
}
