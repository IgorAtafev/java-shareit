package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

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
        UserDto userDto = initUserDto();

        User user = userMapper.toUser(userDto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("user@user.com");
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
