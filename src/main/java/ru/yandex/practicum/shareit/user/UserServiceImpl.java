package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shareit.validator.ConflictException;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public List<User> getUsers() {
        return repository.getUsers();
    }

    @Override
    public User getUserById(Long id) {
        return repository.getUserById(id).orElseThrow(
                () -> new NotFoundException(String.format("User with id %d does not exist", id)));
    }

    @Override
    public User createUser(User user) {
        if (repository.userByEmailExists(user.getEmail(), user.getId())) {
            throw new ConflictException(String.format("User with email %s exists", user.getEmail()));
        }

        return repository.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        if (!repository.userByIdExists(user.getId())) {
            throw new NotFoundException(String.format("User with id %d does not exist", user.getId()));
        }

        if (repository.userByEmailExists(user.getEmail(), user.getId())) {
            throw new ConflictException(String.format("User with email %s exists", user.getEmail()));
        }

        return repository.updateUser(user);
    }

    @Override
    public void removeUserById(Long id) {
        if (!repository.userByIdExists(id)) {
            throw new NotFoundException(String.format("User with id %d does not exist", id));
        }

        repository.removeUserById(id);
    }
}