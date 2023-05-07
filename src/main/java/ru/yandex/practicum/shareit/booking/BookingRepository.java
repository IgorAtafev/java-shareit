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

    /**
     * Returns a list of bookings for item ID and status
     *
     * @param itemId
     * @param status
     * @param sort
     * @return list of bookings
     */
    Collection<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status, Sort sort);

    /**
     * Removes a user's bookings
     *
     * @param userId
     */
    void deleteByBookerId(Long userId);

    /**
     * Removes bookings of all user items
     *
     * @param userId
     */
    void deleteByItemOwnerId(Long userId);
}
