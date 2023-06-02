package ru.yandex.practicum.shareit.request;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRequestService {

    /**
     * Returns a list of requests created by other users
     * Results should be returned page by page
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @param page
     * @return list of requests
     */
    List<ItemRequest> getItemRequestsAll(Long userId, Pageable page);

    /**
     * Returns a list of user requests
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @return list of requests
     */
    List<ItemRequest> getItemRequestsByUserId(Long userId);

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

    /**
     * Sets the list of items to the request
     *
     * @param itemRequest
     * @return
     */
    void setItemsToItemRequest(ItemRequest itemRequest);

    /**
     * Sets a list of items to the list of requests
     *
     * @param itemRequests
     * @return
     */
    void setItemsToItemRequests(List<ItemRequest> itemRequests);
}
