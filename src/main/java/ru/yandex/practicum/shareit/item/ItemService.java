package ru.yandex.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {

    /**
     * Returns a list of user items
     * If the user is not found throws NotFoundException
     *
     * @param userId
     * @return list of user items
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
     * Creates a new item
     * If the user is not found throws NotFoundException
     *
     * @param item
     * @return new item
     */
    Item createItem(Item item);

    /**
     * Updates the item
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
}