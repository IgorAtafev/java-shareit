package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.yandex.practicum.shareit.validator.ValidationOnUpdate;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserClient client;

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return client.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        return client.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createUser(@RequestBody @Validated(ValidationOnCreate.class) UserDto userDto) {
        log.info("Request received POST /users: '{}'", userDto);
        return client.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUserById(
            @PathVariable Long id,
            @RequestBody @Validated(ValidationOnUpdate.class) UserDto userDto
    ) {
        log.info("Request received PATCH /users/{}: '{}'", id, userDto);
        return client.updateUserById(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable Long id) {
        log.info("Request received DELETE /users/{}", id);
        client.removeUserById(id);
    }
}
