package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto userDto) {
        validateEmail(userDto.getEmail(), null);
        User user = userStorage.create(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = getUserOrThrow(userId);
        if (userDto.getName() != null) {
            validateTextField(userDto.getName(), "name");
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateTextField(userDto.getEmail(), "email");
            validateEmail(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }
        return userMapper.toUserDto(userStorage.update(user));
    }

    @Override
    public UserDto getById(Long userId) {
        return userMapper.toUserDto(getUserOrThrow(userId));
    }

    @Override
    public Collection<UserDto> getAll() {
        return userStorage.getAll().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        getUserOrThrow(userId);
        userStorage.delete(userId);
    }

    private void validateEmail(String email, Long currentUserId) {
        userStorage.findByEmail(email)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ConflictException("Email already exists: " + email);
                });
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private void validateTextField(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("User " + fieldName + " must not be blank");
        }
    }
}
