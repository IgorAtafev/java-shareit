package ru.yandex.practicum.shareit.item;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemService {

    /**
     * Returns a list of user's items
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @return list of items
     */
    Collection<Item> getItemsByUserId(Long userId);

    /**
     * Returns item by id
     * If the item is not found throws NotFoundException
     *
     * @param id
     * @return user by id
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
     *
     * @param text
     * @return list of items
     */
    Collection<Item> searchItems(String text);

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
     * @return a list of comments for item IDs
     */
    Map<Long, List<Comment>> getCommentsByItemIds(List<Long> itemIds);

    /**
     * Returns a list of comments for item ID
     *
     * @param itemId
     * @return a list of comments for item ID
     */
    List<Comment> getCommentsByItemId(Long itemId);
}
