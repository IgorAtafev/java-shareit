package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> getUsers() {
        return userMapper.toDtos(userService.getUsers());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userMapper.toDto(userService.getUserById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Request received POST /users: '{}'", userDto);
        User user = userMapper.toUser(userDto);
        return userMapper.toDto(userService.createUser(user));
    }

    @PatchMapping("/{id}")
    public UserDto updateUserById(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("Request received PATCH /users/{}: '{}'", id, userDto);
        userDto.setId(id);
        return userMapper.toDto(userService.updateUser(toUser(userDto)));
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable Long id) {
        log.info("Request received DELETE /users/{}", id);
        userService.removeUserById(id);
    }

    private User toUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        User oldUser = userService.getUserById(userDto.getId());

        if (userDto.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
        if (userDto.getName() == null) {
            user.setName(oldUser.getName());
        }

        return user;
    }
}
