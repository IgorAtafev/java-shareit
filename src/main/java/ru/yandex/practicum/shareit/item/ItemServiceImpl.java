package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<Item> getItemsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d does not exist", userId));
        }

        return itemRepository.findByOwnerIdOrderById(userId);
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Item with id %d does not exist", id)));
    }

    @Override
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Item item) {
        if (!itemRepository.existsByIdAndOwnerId(item.getId(), item.getOwner().getId())) {
            throw new NotFoundException(String.format("Item with id %d and user id %d does not exist",
                    item.getId(), item.getOwner().getId()));
        }

        return itemRepository.save(item);
    }

    @Override
    public Collection<Item> searchItems(String text) {
        return itemRepository.searchItemsByText(text);
    }
}
