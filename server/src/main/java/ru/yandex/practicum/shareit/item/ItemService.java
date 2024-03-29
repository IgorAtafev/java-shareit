package ru.yandex.practicum.shareit.item;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ItemService {

    /**
     * Returns a list of user's items
     * Results should be returned page by page
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @param page
     * @return list of items
     */
    List<Item> getItemsByUserId(Long userId, Pageable page);

    /**
     * Returns item by id
     * If the item is not found throws NotFoundException
     *
     * @param id
     * @return item by id
     */
    Item getItemById(Long id);

    /**
     * Creates a new item by the user
     * If the user is not found throws NotFoundException
     *
     * @param item
     * @return new item
     */
    Item createItem(Item item);

    /**
     * Updates the item by the user
     * If the user is not found throws NotFoundException
     * If the item is not found throws NotFoundException
     *
     * @param item
     * @return updated item
     */
    Item updateItem(Item item);

    /**
     * Returns a list of found items available for rent
     * The search is conducted by the presence of a substring text in the title and description
     * Results should be returned page by page
     *
     * @param text
     * @param page
     * @return list of items
     */
    List<Item> searchItems(String text, Pageable page);

    /**
     * Creates a new comment for the item by the user
     * If the user is not found throws NotFoundException
     * If the item is not found throws NotFoundException
     * If the user did not rent the item or the user's lease period
     * has not expired throws ValidationException
     *
     * @param comment
     * @return new comment
     */
    Comment createComment(Comment comment);

    /**
     * Returns a list of comments for item IDs
     *
     * @param itemIds
     * @return list of comments
     */
    Map<Long, List<Comment>> getCommentsByItemIds(List<Long> itemIds);

    /**
     * Returns a list of comments for item ID
     *
     * @param itemId
     * @return list of comments
     */
    List<Comment> getCommentsByItemId(Long itemId);

    /**
     * Sets the list of bookings and comments to the list of items
     *
     * @param items
     * @return
     */
    void setBookingsAndCommentsToItems(List<Item> items);

    /**
     * Sets the list of bookings and comments for the item
     *
     * @param item
     * @return
     */
    void setBookingsAndCommentsToItem(Item item);

    /**
     * Sets the list of comments for the item
     *
     * @param item
     * @return
     */
    void setCommentsToItem(Item item);

    /**
     * Returns a list of items for request IDs
     *
     * @param requestIds
     * @return list of items
     */
    Map<Long, List<Item>> getItemsByRequestIds(List<Long> requestIds);

    /**
     * Returns a list of items for request ID
     *
     * @param requestId
     * @return list of items
     */
    List<Item> getItemsByRequestId(Long requestId);
}
