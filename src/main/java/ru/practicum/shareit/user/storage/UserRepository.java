package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import ru.practicum.shareit.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
