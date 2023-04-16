package ru.yandex.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 0;

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User createUser(User user) {
        user.setId(++nextId);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void removeUserById(Long id) {
        users.remove(id);
    }

    @Override
    public boolean userByIdExists(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean userByEmailExists(String email, Long id) {
       return users.values().stream()
               .anyMatch(user -> !Objects.equals(id, user.getId()) && Objects.equals(email, user.getEmail()));
    }
}