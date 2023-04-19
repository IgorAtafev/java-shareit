package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserService userService;

    public UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());

        return userDto;
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