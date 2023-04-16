package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shareit.item.ItemRepository;
import ru.yandex.practicum.shareit.validator.ConflictException;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Collection<User> getUsers() {
        return userRepository.getUsers();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.getUserById(id).orElseThrow(
                () -> new NotFoundException(String.format("User with id %d does not exist", id)));
    }

    @Override
    public User createUser(User user) {
        if (userRepository.userByEmailExists(user.getEmail(), user.getId())) {
            throw new ConflictException(String.format("User with email %s exists", user.getEmail()));
        }

        return userRepository.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        if (!userRepository.userByIdExists(user.getId())) {
            throw new NotFoundException(String.format("User with id %d does not exist", user.getId()));
        }

        if (userRepository.userByEmailExists(user.getEmail(), user.getId())) {
            throw new ConflictException(String.format("User with email %s exists", user.getEmail()));
        }

        return userRepository.updateUser(user);
    }

    @Override
    public void removeUserById(Long id) {
        if (!userRepository.userByIdExists(id)) {
            throw new NotFoundException(String.format("User with id %d does not exist", id));
        }

        itemRepository.removeItemsByUserId(id);
        userRepository.removeUserById(id);
    }
}