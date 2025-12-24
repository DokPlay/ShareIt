package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_Success() {
        UserDto userDto = new UserDto(null, "Test User", "test@email.com");

        UserDto created = userService.create(userDto);

        assertNotNull(created.getId());
        assertEquals("Test User", created.getName());
        assertEquals("test@email.com", created.getEmail());
    }

    @Test
    void createUser_DuplicateEmail_ThrowsConflict() {
        UserDto user1 = new UserDto(null, "User 1", "same@email.com");
        userService.create(user1);

        UserDto user2 = new UserDto(null, "User 2", "same@email.com");

        assertThrows(ConflictException.class, () -> userService.create(user2));
    }

    @Test
    void createUser_BlankName_ThrowsValidation() {
        UserDto userDto = new UserDto(null, "", "test@email.com");

        assertThrows(ValidationException.class, () -> userService.create(userDto));
    }

    @Test
    void createUser_BlankEmail_ThrowsValidation() {
        UserDto userDto = new UserDto(null, "Test User", "");

        assertThrows(ValidationException.class, () -> userService.create(userDto));
    }

    @Test
    void updateUser_Success() {
        UserDto userDto = new UserDto(null, "Original", "original@email.com");
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto(null, "Updated", "updated@email.com");
        UserDto updated = userService.update(created.getId(), updateDto);

        assertEquals("Updated", updated.getName());
        assertEquals("updated@email.com", updated.getEmail());
    }

    @Test
    void updateUser_PartialUpdate_Success() {
        UserDto userDto = new UserDto(null, "Original", "original@email.com");
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto(null, "Updated Name", null);
        UserDto updated = userService.update(created.getId(), updateDto);

        assertEquals("Updated Name", updated.getName());
        assertEquals("original@email.com", updated.getEmail());
    }

    @Test
    void updateUser_NotFound_ThrowsNotFound() {
        UserDto updateDto = new UserDto(null, "Updated", "updated@email.com");

        assertThrows(NotFoundException.class, () -> userService.update(999L, updateDto));
    }

    @Test
    void getById_Success() {
        UserDto userDto = new UserDto(null, "Test User", "test@email.com");
        UserDto created = userService.create(userDto);

        UserDto found = userService.getById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Test User", found.getName());
    }

    @Test
    void getById_NotFound_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_Success() {
        userService.create(new UserDto(null, "User 1", "user1@email.com"));
        userService.create(new UserDto(null, "User 2", "user2@email.com"));

        List<UserDto> users = userService.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void delete_Success() {
        UserDto userDto = new UserDto(null, "Test User", "test@email.com");
        UserDto created = userService.create(userDto);

        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }

    @Test
    void delete_NotFound_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.delete(999L));
    }
}
