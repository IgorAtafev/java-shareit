package ru.yandex.practicum.shareit.booking;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class BookingSpecification {

    public static Specification<Booking> byBookerId(Long userId) {
        return (root, query, builder) ->
                builder.equal(root.<Long>get("booker").get("id"), userId);
    }

    public static Specification<Booking> byItemOwnerId(Long userId) {
        return (root, query, builder) -> builder.equal(root.<Long>get("item")
                .get("owner").get("id"), userId);
    }

    public static Specification<Booking> hasCurrent() {
        return (root, query, builder) -> {
            LocalDateTime now = LocalDateTime.now();
            return builder.and(
                    builder.lessThan(root.get("start"), now),
                    builder.greaterThan(root.get("end"), now)
            );
        };
    }

    public static Specification<Booking> hasPast() {
        return (root, query, builder) -> {
            LocalDateTime now = LocalDateTime.now();
            return builder.lessThan(root.get("end"), now);
        };
    }

    public static Specification<Booking> hasFuture() {
        return (root, query, builder) -> {
            LocalDateTime now = LocalDateTime.now();
            return builder.greaterThan(root.get("start"), now);
        };
    }

    public static Specification<Booking> hasWaiting() {
        return (root, query, builder) -> builder.equal(root.get("status"), BookingStatus.WAITING);
    }

    public static Specification<Booking> hasRejected() {
        return (root, query, builder) -> builder.equal(root.get("status"), BookingStatus.REJECTED);
    }
}
