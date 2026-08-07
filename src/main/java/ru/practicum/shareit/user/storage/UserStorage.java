package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    Collection<User> getAll();

    void delete(Long userId);
}
