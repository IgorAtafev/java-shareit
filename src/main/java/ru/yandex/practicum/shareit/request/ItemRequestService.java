package ru.yandex.practicum.shareit.request;

import java.util.Collection;

public interface ItemRequestService {

    /**
     * Returns a list of requests created by other users
     * Results should be returned page by page
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @param from
     * @param size
     * @return list of requests
     */
    Collection<ItemRequest> getItemRequestsAll(Long userId, Integer from, Integer size);

    /**
     * Returns a list of user requests
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @return list of requests
     */
    Collection<ItemRequest> getItemRequestsByUserId(Long userId);

    /**
     * Returns an item request by id
     * If the request is not found throws NotFoundException
     *
     * @param id
     * @param userId
     * @return item request by id
     */
    ItemRequest getItemRequestById(Long id, Long userId);

    /**
     * Creates a new item request by the user
     * If the user is not found throws NotFoundException
     *
     * @param itemRequest
     * @return new item request
     */
    ItemRequest createRequest(ItemRequest itemRequest);
}
