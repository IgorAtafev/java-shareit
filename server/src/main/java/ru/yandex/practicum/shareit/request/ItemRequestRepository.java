package ru.yandex.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    /**
     * Returns a list of requests created by other users
     * Results should be returned page by page
     *
     * @param userId
     * @param page
     * @return list of requests
     */
    List<ItemRequest> findByRequestorIdNot(Long userId, Pageable page);

    /**
     * Returns a list of user requests
     *
     * @param requesterId
     * @param sort
     * @return list of requests
     */
    List<ItemRequest> findByRequestorId(Long requesterId, Sort sort);
}
