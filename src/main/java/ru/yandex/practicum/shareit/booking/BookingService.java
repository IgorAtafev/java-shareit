package ru.yandex.practicum.shareit.booking;

import java.util.List;
import java.util.Map;

public interface BookingService {

    /**
     * Returns a list of user's bookings
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @param state
     * @return list of bookings
     */
    Iterable<Booking> getBookingsByUserId(Long userId, String state);

    /**
     * Returns a list of bookings for all the user's items
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @param state
     * @return list of bookings
     */
    Iterable<Booking> getBookingsByItemOwnerId(Long userId, String state);

    /**
     * Returns booking by id
     * Can be done either by the author of the booking or
     * by the owner of the item to which the booking relates
     * If the booking is not found throws NotFoundException
     *
     * @param id
     * @param userId
     * @return booking by id
     */
    Booking getBookingById(Long id, Long userId);

    /**
     * Creates a new item booking by the user
     * If the user is not found throws NotFoundException
     * If the item is not found throws NotFoundException
     *
     * @param booking
     * @return new booking
     */
    Booking createBooking(Booking booking);

    /**
     * Approve or reject item booking
     * Can only be performed by the owner of the item
     * If the booking is not found throws NotFoundException
     *
     * @param id
     * @param approved
     * @param userId
     * @return approved or rejected booking
     */
    Booking approveBookingById(Long id, Boolean approved, Long userId);

    /**
     * Returns a list of bookings for item IDs
     *
     * @param itemIds
     * @return list of bookings
     */
    Map<Long, List<Booking>> getBookingsByItemIds(List<Long> itemIds);

    /**
     * Returns a list of bookings for item ID
     *
     * @param itemId
     * @return list of bookings
     */
    List<Booking> getBookingsByItemId(Long itemId);

    /**
     * Returns the last booking before the current time
     *
     * @param bookings
     * @return last booking
     */
    Booking getLastBooking(List<Booking> bookings);

    /**
     * Returns the first booking after the current time
     *
     * @param bookings
     * @return first booking
     */
    Booking getNextBooking(List<Booking> bookings);
}
