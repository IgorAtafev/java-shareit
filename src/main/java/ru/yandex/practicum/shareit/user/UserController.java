package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@Validated
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
    @Validated(ValidationOnCreate.class)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        log.info("Request received POST /users: '{}'", userDto);
        return userMapper.toDto(userService.createUser(userMapper.toUser(userDto)));
    }

    @PatchMapping("/{id}")
    public UserDto updateUserById(@PathVariable Long id, @RequestBody @Valid UserDto userDto) {
        log.info("Request received PATCH /users/{}: '{}'", id, userDto);
        userDto.setId(id);
        return userMapper.toDto(userService.updateUser(userMapper.toUser(userDto)));
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable Long id) {
        log.info("Request received DELETE /users/{}", id);
        userService.removeUserById(id);
    }
}
