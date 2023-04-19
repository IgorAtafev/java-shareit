package ru.yandex.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    /**
     * Returns a list of all users
     *
     * @return list of all users
     */
    Collection<User> getUsers();

    /**
     * Returns user by id
     *
     * @param id
     * @return user or null if there was no one
     */
    Optional<User> getUserById(Long id);

    /**
     * Creates a new user
     *
     * @param user
     * @return new user
     */
    User createUser(User user);

    /**
     * Updates the user
     *
     * @param user
     * @return updated user
     */
    User updateUser(User user);

    /**
     * Removes a user
     *
     * @param id
     */
    void removeUserById(Long id);

    /**
     * Checks for the existence of user by id
     *
     * @param id
     * @return true or false
     */
    boolean userByIdExists(Long id);

    /**
     * Checks for the existence of an email user other than the given user
     *
     * @param email
     * @param id
     * @return true or false
     */
    boolean userByEmailExists(String email, Long id);
}
