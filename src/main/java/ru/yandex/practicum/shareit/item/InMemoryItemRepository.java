package ru.yandex.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Set<Item>> items = new HashMap<>();
    private long nextId = 0;

    @Override
    public Collection<Item> getItemsByUserId(Long userId) {
        return items.get(userId);
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        return items.values().stream()
                .flatMap(Set::stream)
                .filter(item -> Objects.equals(id, item.getId()))
                .findFirst();
    }

    @Override
    public Item createItem(Item item) {
        Long userId = item.getOwner().getId();

        if (!items.containsKey(userId)) {
            items.put(userId, new HashSet<>());
        }

        item.setId(++nextId);
        items.get(userId).add(item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Long userId = item.getOwner().getId();
        Set<Item> userItems = items.get(userId);

        userItems.stream()
                .filter(removableItem -> Objects.equals(item.getId(), removableItem.getId()))
                .findFirst()
                .ifPresent(userItems::remove);

        userItems.add(item);
        items.put(userId, userItems);
        return item;
    }

    @Override
    public boolean itemByIdAndUserIdExists(Long id, Long userId) {
        if (!items.containsKey(userId)) {
            return false;
        }

        return items.get(userId).stream()
                .anyMatch(item -> Objects.equals(id, item.getId()));
    }
}