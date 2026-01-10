package ru.practicum.shareit.user;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing user CRUD operations.
 */
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Registers a new user.
   */
  @PostMapping
  public UserDto create(@RequestBody UserDto userDto) {
    return userService.create(userDto);
  }

  /**
   * Updates provided fields of an existing user.
   */
  @PatchMapping("/{userId}")
  public UserDto update(@PathVariable long userId, @RequestBody UserDto userDto) {
    return userService.update(userId, userDto);
  }

  /**
   * Retrieves a single user by id.
   */
  @GetMapping("/{userId}")
  public UserDto getById(@PathVariable long userId) {
    return userService.getById(userId);
  }

  /**
   * Lists all users ordered by storage implementation.
   */
  @GetMapping
  public List<UserDto> getAll() {
    return userService.getAll();
  }

  /**
   * Removes a user permanently.
   */
  @DeleteMapping("/{userId}")
  public void delete(@PathVariable long userId) {
    userService.delete(userId);
  }
}