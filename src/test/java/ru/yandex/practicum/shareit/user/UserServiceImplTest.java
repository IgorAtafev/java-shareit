package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.item.ItemRepository;
import ru.yandex.practicum.shareit.validator.ConflictException;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsers_shouldReturnEmptyListOfUsers() {
        when(userRepository.getUsers()).thenReturn(Collections.emptyList());

        assertTrue(userService.getUsers().isEmpty());

        verify(userRepository, times(1)).getUsers();
    }

    @Test
    void getUsers_shouldReturnListOfUsers() {
        User user1 = initUser();
        User user2 = initUser();

        List<User> expected = List.of(user1, user2);

        when(userRepository.getUsers()).thenReturn(expected);

        assertEquals(expected, userService.getUsers());

        verify(userRepository, times(1)).getUsers();
    }

    @Test
    void getUserById_shouldReturnUserById() {
        Long userId = 1L;
        User user = initUser();

        when(userRepository.getUserById(userId)).thenReturn(Optional.of(user));

        assertEquals(user, userService.getUserById(userId));

        verify(userRepository, times(1)).getUserById(userId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void getUserById_shouldThrowAnException_ifUserDoesNotExist(Long userId) {
        when(userRepository.getUserById(userId)).thenThrow(NotFoundException.class);

        assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(userId)
        );

        verify(userRepository, times(1)).getUserById(userId);
    }

    @Test
    void createUser_shouldCreateAUser() {
        User user = initUser();

        when(userRepository.userByEmailExists(user.getEmail(), user.getId())).thenReturn(false);
        when(userRepository.createUser(user)).thenReturn(user);

        assertEquals(user, userService.createUser(user));

        verify(userRepository, times(1)).userByEmailExists(user.getEmail(), user.getId());
        verify(userRepository, times(1)).createUser(user);
    }

    @Test
    void createUser_shouldThrowAnException_ifTheUserEmailExists() {
        User user = initUser();

        when(userRepository.userByEmailExists(user.getEmail(), user.getId())).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> userService.createUser(user)
        );

        verify(userRepository, times(1)).userByEmailExists(user.getEmail(), user.getId());
        verify(userRepository, never()).createUser(user);
    }

    @Test
    void updateUser_shouldUpdateTheUser() {
        Long userId = 1L;
        User user = initUser();
        user.setId(userId);

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(userRepository.userByEmailExists(user.getEmail(), user.getId())).thenReturn(false);
        when(userRepository.updateUser(user)).thenReturn(user);

        assertEquals(user, userService.updateUser(user));

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(userRepository, times(1)).userByEmailExists(user.getEmail(), user.getId());
        verify(userRepository, times(1)).updateUser(user);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void updateUser_shouldThrowAnException_ifUserDoesNotExist(Long userId) {
        User user = initUser();
        user.setId(userId);

        when(userRepository.userByIdExists(userId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(user)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(userRepository, never()).userByEmailExists(user.getEmail(), user.getId());
        verify(userRepository, never()).updateUser(user);
    }

    @Test
    void updateUser_shouldThrowAnException_ifTheUserEmailExists() {
        Long userId = 1L;
        User user = initUser();
        user.setId(userId);

        when(userRepository.userByIdExists(userId)).thenReturn(true);
        when(userRepository.userByEmailExists(user.getEmail(), user.getId())).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> userService.updateUser(user)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(userRepository, times(1)).userByEmailExists(user.getEmail(), user.getId());
        verify(userRepository, never()).createUser(user);
    }

    @Test
    void removeUserById_shouldRemoveTheUser() {
        Long userId = 1L;

        when(userRepository.userByIdExists(userId)).thenReturn(true);

        userService.removeUserById(userId);

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, times(1)).removeItemsByUserId(userId);
        verify(userRepository, times(1)).removeUserById(userId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void removeUserById_shouldThrowAnException_ifUserDoesNotExist(Long userId) {
        Long friendId = 2L;

        when(userRepository.userByIdExists(userId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> userService.removeUserById(userId)
        );

        verify(userRepository, times(1)).userByIdExists(userId);
        verify(itemRepository, never()).removeItemsByUserId(userId);
        verify(userRepository, never()).removeUserById(userId);
    }

    private static User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}