package ru.yandex.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /**
     * Returns a list of bookings for item IDs and status
     *
     * @param itemIds
     * @param status
     * @param sort
     * @return list of bookings
     */
    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, BookingStatus status, Sort sort);

    /**
     * Returns a list of bookings for item ID and status
     *
     * @param itemId
     * @param status
     * @param sort
     * @return list of bookings
     */
    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status, Sort sort);

    /**
     * Checks for a booking by item id, user id, status, and rental expiration date
     *
     * @param itemId
     * @param userId
     * @param status
     * @param dateTime
     * @return true or false
     */
    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long userId, BookingStatus status, LocalDateTime dateTime
    );

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
