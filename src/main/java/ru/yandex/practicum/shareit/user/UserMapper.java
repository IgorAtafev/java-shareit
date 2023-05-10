package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserService userService;

    public UserDto toDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());

        return userDto;
    }

    public List<UserDto> toDtos(Collection<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public User toUser(UserDto userDto) {
        User user = new User();

        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());

        if (userDto.getId() != null) {
            User oldUser = userService.getUserById(userDto.getId());

            if (userDto.getEmail() == null) {
                user.setEmail(oldUser.getEmail());
            }
            if (userDto.getName() == null) {
                user.setName(oldUser.getName());
            }
        }

        return user;
    }
}
