package ru.yandex.practicum.shareit.item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    /**
     * Returns a list of user items
     *
     * @param userId
     * @return list of user items
     */
    Collection<Item> getItemsByUserId(Long userId);

    /**
     * Returns item by id
     *
     * @param id
     * @return item or null if there was no one
     */
    Optional<Item> getItemById(Long id);

    /**
     * Creates a new item
     *
     * @param item
     * @return new item
     */
    Item createItem(Item item);

    /**
     * Updates the item
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
     * Checks for the existence of item by id and user id
     *
     * @param id
     * @param userId
     * @return true or false
     */
    boolean itemByIdAndUserIdExists(Long id, Long userId);
}