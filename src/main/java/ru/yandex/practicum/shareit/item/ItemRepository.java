package ru.yandex.practicum.shareit.item;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Returns a list of user items
     *
     * @param ownerId
     * @return list of items
     */
    Collection<Item> findByOwnerIdOrderById(Long ownerId);

    /**
     * Returns a list of found items available for rent
     * The search is conducted by the presence of a substring text in the title and description
     *
     * @param text
     * @return list of items
     */
    @Query("select i " +
            "from Item i " +
            "where i.available = true " +
            "and (upper(i.name) like upper(concat('%', ?1,'%')) " +
            "or upper(i.description) like upper(concat('%', ?1,'%'))) " +
            "order by i.id")
    Collection<Item> searchItemsByText(String text);

    /**
     * Checks for the existence of item by id and user id
     *
     * @param id
     * @param ownerId
     * @return true or false
     */
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Removes user items
     *
     * @param ownerId
     */
    void deleteByOwnerId(Long ownerId);

    /**
     * Returns a list of items for request IDs
     *
     * @param requestIds
     * @param sort
     * @return list of items
     */
    Collection<Item> findByRequestIdIn(List<Long> requestIds, Sort sort);

    /**
     * Returns a list of items for request ID
     *
     * @param requestId
     * @param sort
     * @return list of items
     */
    Collection<Item> findByRequestId(Long requestId, Sort sort);
}
