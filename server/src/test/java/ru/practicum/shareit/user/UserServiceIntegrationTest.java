package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
            .name("Alice")
            .email("alice@example.com")
            .build());
    }

    @Test
    void createShouldPersistUser() {
        UserDto created = userService.create(UserDto.builder()
            .name("Bob")
            .email("bob@example.com")
            .build());
        assertThat(created.getId()).isNotNull();
        assertThat(userRepository.findById(created.getId()))
            .get()
            .extracting(User::getEmail)
            .isEqualTo("bob@example.com");
    }

    @Test
    void updateShouldChangeOnlyProvidedFields() {
        UserDto updated = userService.update(savedUser.getId(), UserDto.builder()
            .name("Alicia")
            .build());
        assertThat(updated.getName()).isEqualTo("Alicia");
        assertThat(updated.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void getByIdShouldReadUser() {
        UserDto found = userService.getById(savedUser.getId());
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void getAllShouldReadUsers() {
        assertThat(userService.getAll())
            .extracting(UserDto::getId)
            .contains(savedUser.getId());
    }

    @Test
    void deleteShouldRemoveUser() {
        userService.delete(savedUser.getId());
        assertThat(userRepository.existsById(savedUser.getId())).isFalse();
    }
}
