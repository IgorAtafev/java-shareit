package ru.yandex.practicum.shareit.item;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Returns a list of comments for item IDs
     *
     * @param itemIds
     * @param sort
     * @return list of bookings
     */
    Collection<Comment> findByItemIdIn(List<Long> itemIds, Sort sort);

    /**
     * Returns a list of comments for item ID
     *
     * @param itemId
     * @param sort
     * @return list of comments
     */
    Collection<Comment> findByItemId(Long itemId, Sort sort);

    /**
     * Removes a user's comments
     *
     * @param userId
     */
    void deleteByAuthorId(Long userId);
}
