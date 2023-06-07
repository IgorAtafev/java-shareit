package ru.yandex.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.booking.BookingRepository;
import ru.yandex.practicum.shareit.item.CommentRepository;
import ru.yandex.practicum.shareit.item.ItemRepository;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("User with id %d does not exist", id)));
    }

    @Transactional
    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void removeUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(String.format("User with id %d does not exist", id));
        }

        bookingRepository.deleteByItemOwnerId(id);
        itemRepository.deleteByOwnerId(id);
        bookingRepository.deleteByBookerId(id);
        commentRepository.deleteByAuthorId(id);
        userRepository.deleteById(id);
    }
}
