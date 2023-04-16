package ru.yandex.practicum.shareit.user;

import java.util.Collection;

public interface UserService {

    /**
     * Returns a list of all users
     *
     * @return list of all users
     */
    Collection<User> getUsers();

    /**
     * Returns user by id
     * If the user is not found throws NotFoundException
     *
     * @param id
     * @return user by id
     */
    User getUserById(Long id);

    /**
     * Creates a new user
     * If the user's email already exists throws ConflictException
     *
     * @param user
     * @return new user
     */
    User createUser(User user);

    /**
     * Updates the user
     * If the user is not found throws NotFoundException
     * If the user's email already exists throws ConflictException
     *
     * @param user
     * @return updated user
     */
    User updateUser(User user);

    /**
     * Removes a user
     * If the user is not found throws NotFoundException
     *
     * @param id
     */
    void removeUserById(Long id);
}