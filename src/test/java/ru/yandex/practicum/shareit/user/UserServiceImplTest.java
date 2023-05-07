package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.booking.BookingRepository;
import ru.yandex.practicum.shareit.item.CommentRepository;
import ru.yandex.practicum.shareit.item.ItemRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
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

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsers_shouldReturnEmptyListOfUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertThat(userService.getUsers().isEmpty()).isTrue();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUsers_shouldReturnListOfUsers() {
        User user1 = initUser();
        User user2 = initUser();

        List<User> expected = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(expected);

        assertThat(userService.getUsers()).isEqualTo(expected);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_shouldReturnUserById() {
        Long userId = 1L;
        User user = initUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThat(userService.getUserById(userId)).isEqualTo(user);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenThrow(NotFoundException.class);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> userService.getUserById(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void createUser_shouldCreateAUser() {
        User user = initUser();

        when(userRepository.save(user)).thenReturn(user);

        assertThat(userService.createUser(user)).isEqualTo(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_shouldUpdateTheUser() {
        Long userId = 1L;
        User user = initUser();
        user.setId(userId);

        when(userRepository.save(user)).thenReturn(user);

        assertThat(userService.updateUser(user)).isEqualTo(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void removeUserById_shouldRemoveTheUser() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.removeUserById(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(bookingRepository, times(1)).deleteByItemOwnerId(userId);
        verify(itemRepository, times(1)).deleteByOwnerId(userId);
        verify(bookingRepository, times(1)).deleteByBookerId(userId);
        verify(commentRepository, times(1)).deleteByAuthorId(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void removeUserById_shouldThrowAnException_ifUserDoesNotExist() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> userService.removeUserById(userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(bookingRepository, never()).deleteByItemOwnerId(userId);
        verify(itemRepository, never()).deleteByOwnerId(userId);
        verify(bookingRepository, never()).deleteByBookerId(userId);
        verify(commentRepository, never()).deleteByAuthorId(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    private static User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}
