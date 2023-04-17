package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItemsByUserId_shouldReturnEmptyListOfItems() {
        Long userId = 1L;

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(itemRepository.getItemsByUserId(userId)).thenReturn(Collections.emptyList());

        assertTrue(itemService.getItemsByUserId(userId).isEmpty());

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, times(1)).getItemsByUserId(userId);
    }

    @Test
    void getItemsByUserId_shouldReturnItemsByUserId() {
        Long userId = 1L;
        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> expected = List.of(item1, item2);

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(itemRepository.getItemsByUserId(userId)).thenReturn(expected);

        assertEquals(expected, itemService.getItemsByUserId(userId));

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, times(1)).getItemsByUserId(userId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void getItemsByUserId_shouldThrowAnException_ifUserDoesNotExist(Long userId) {
        when(userRepository.userByIdExists(userId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> itemService.getItemsByUserId(userId)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, never()).getItemsByUserId(userId);
    }

    @Test
    void getItemById_shouldReturnUserById() {
        Long itemId = 1L;
        Item item = initItem();

        when(itemRepository.getItemById(itemId)).thenReturn(Optional.of(item));

        assertEquals(item, itemService.getItemById(itemId));

        verify(itemRepository, times(1)).getItemById(itemId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void getItemById_shouldThrowAnException_ifUserDoesNotExist(Long itemId) {
        when(itemRepository.getItemById(itemId)).thenThrow(NotFoundException.class);

        assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(itemId)
        );

        verify(itemRepository, times(1)).getItemById(itemId);
    }

    @Test
    void createItem_shouldCreateAItem() {
        Long userId = 1L;
        Item item = initItem();
        item.getOwner().setId(userId);

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(itemRepository.createItem(item)).thenReturn(item);

        assertEquals(item, itemService.createItem(item));

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, times(1)).createItem(item);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void createItem_shouldThrowAnException_ifUserDoesNotExist(Long userId) {
        Item item = initItem();
        item.getOwner().setId(userId);

        when(userRepository.userByIdExists(userId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> itemService.createItem(item)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, never()).createItem(item);
    }

    @Test
    void updateItem_shouldUpdateTheItem() {
        Long userId = 1L;
        Long itemId = 2L;
        Item item = initItem();
        item.getOwner().setId(userId);
        item.setId(itemId);

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(itemRepository.itemByIdAndUserIdExists(itemId, userId)).thenReturn(true);
        when(itemRepository.updateItem(item)).thenReturn(item);

        assertEquals(item, itemService.updateItem(item));

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, times(1)).itemByIdAndUserIdExists(itemId, userId);
        verify(itemRepository, times(1)).updateItem(item);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void updateItem_shouldThrowAnException_ifUserDoesNotExist(Long userId) {
        Long itemId = 2L;
        Item item = initItem();
        item.getOwner().setId(userId);
        item.setId(itemId);

        when(userRepository.userByIdExists(userId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(item)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, never()).itemByIdAndUserIdExists(itemId, userId);
        verify(itemRepository, never()).updateItem(item);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void updateItem_shouldThrowAnException_ifItemDoesNotExist(Long itemId) {
        Long userId = 1L;
        Item item = initItem();
        item.getOwner().setId(userId);
        item.setId(itemId);

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(itemRepository.itemByIdAndUserIdExists(itemId, userId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(item)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, times(1)).itemByIdAndUserIdExists(itemId, userId);
        verify(itemRepository, never()).updateItem(item);
    }

    @Test
    void searchItems_shouldReturnEmptyListOfItems() {
        String text = "аккумулятор";

        when(itemRepository.searchItems(text)).thenReturn(Collections.emptyList());

        assertTrue(itemService.searchItems(text).isEmpty());

        verify(itemRepository, times(1)).searchItems(text);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentInTheNameAndDescription() {
        String text = "дрель";
        Item item1 = initItem();
        Item item2 = initItem();

        List<Item> expected = List.of(item1, item2);

        when(itemRepository.searchItems(text)).thenReturn(expected);

        assertEquals(expected, itemService.searchItems(text));

        verify(itemRepository, times(1)).searchItems(text);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentOnlyInTheName() {
        String text = "дрель";
        Item item1 = initItem();
        Item item2 = initItem();

        item1.setName("Аккумулятор");
        item1.setDescription("Аккумулятор");
        item2.setDescription("Простая");

        List<Item> expected = List.of(item2);

        when(itemRepository.searchItems(text)).thenReturn(expected);

        assertEquals(expected, itemService.searchItems(text));

        verify(itemRepository, times(1)).searchItems(text);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentOnlyInTheDescription() {
        String text = "прост";
        Item item1 = initItem();
        Item item2 = initItem();

        item2.setName("Аккумулятор");
        item2.setDescription("Аккумулятор");

        List<Item> expected = List.of(item1);

        when(itemRepository.searchItems(text)).thenReturn(expected);

        assertEquals(expected, itemService.searchItems(text));

        verify(itemRepository, times(1)).searchItems(text);
    }

    private static Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setOwner(new User());

        return item;
    }
}