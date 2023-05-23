package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void toDto_shouldReturnUserDto() {
        User user = initUser();

        UserDto userDto = userMapper.toDto(user);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getEmail()).isEqualTo("user@user.com");
        assertThat(userDto.getName()).isEqualTo("user");
    }

    @Test
    void toDtos_shouldReturnEmptyListOfUserDtos() {
        assertThat(userMapper.toDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void toDtos_shouldReturnListOfUserDtos() {
        User user1 = initUser();
        User user2 = initUser();
        UserDto userDto1 = initUserDto();
        UserDto userDto2 = initUserDto();

        List<UserDto> expected = List.of(userDto1, userDto2);

        assertThat(userMapper.toDtos(List.of(user1, user2))).isEqualTo(expected);
    }

    @Test
    void toUser_shouldReturnUser() {
        String userEmail = "user@user.com";
        String userName = "user";
        UserDto userDto = new UserDto();

        userDto.setEmail(userEmail);
        userDto.setName(userName);

        User user = userMapper.toUser(userDto);

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo(userEmail);
        assertThat(user.getName()).isEqualTo(userName);

        Long userId = 1L;
        String updatedEmail = "user@user.com";
        String updatedName = "updateName";

        userDto.setId(userId);
        userDto.setEmail(updatedEmail);
        userDto.setName(updatedName);

        User oldUser = initUser();
        oldUser.setId(userId);

        when(userService.getUserById(userId)).thenReturn(oldUser);

        user = userMapper.toUser(userDto);

        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo(updatedEmail);
        assertThat(user.getName()).isEqualTo(updatedName);

        verify(userService, times(1)).getUserById(userId);

        userDto.setEmail(null);
        user = userMapper.toUser(userDto);
        assertThat(user.getEmail()).isEqualTo("user@user.com");

        userDto.setName(null);
        user = userMapper.toUser(userDto);
        assertThat(user.getName()).isEqualTo("user");
    }

    private UserDto initUserDto() {
        UserDto userDto = new UserDto();

        userDto.setId(1L);
        userDto.setEmail("user@user.com");
        userDto.setName("user");

        return userDto;
    }

    private User initUser() {
        User user = new User();

        user.setId(1L);
        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}
