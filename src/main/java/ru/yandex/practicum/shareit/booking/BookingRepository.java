package ru.yandex.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    /**
     * Returns a list of bookings for item IDs and status
     *
     * @param itemIds
     * @param status
     * @param sort
     * @return list of bookings
     */
    Collection<Booking> findByItemIdInAndStatus(List<Long> itemIds, BookingStatus status, Sort sort);
}
