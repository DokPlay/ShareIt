package ru.practicum.shareit.user;

import java.util.List;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;

/**
 * Service layer handling validation and interaction with user storage.
 */
@Service
public class UserServiceImpl implements UserService {

  private final UserStorage userStorage;

  public UserServiceImpl(UserStorage userStorage) {
    this.userStorage = userStorage;
  }

  @Override
  /**
   * Creates a user after validating required fields.
   */
  public UserDto create(UserDto userDto) {
    validateCreate(userDto);
    User created = userStorage.create(UserMapper.toUser(userDto));
    return UserMapper.toUserDto(created);
  }

  @Override
  /**
   * Applies partial updates to an existing user.
   */
  public UserDto update(long userId, UserDto userDto) {
    User existing = userStorage.getById(userId);

    if (userDto.getName() != null) {
      existing.setName(userDto.getName());
    }
    if (userDto.getEmail() != null) {
      existing.setEmail(userDto.getEmail());
    }

    User updated = userStorage.update(existing);
    return UserMapper.toUserDto(updated);
  }

  @Override
  /**
   * Returns user details by id.
   */
  public UserDto getById(long userId) {
    return UserMapper.toUserDto(userStorage.getById(userId));
  }

  @Override
  /**
   * Lists all users currently stored.
   */
  public List<UserDto> getAll() {
    return userStorage.getAll().stream().map(UserMapper::toUserDto).toList();
  }

  @Override
  /**
   * Deletes a user record by id.
   */
  public void delete(long userId) {
    userStorage.delete(userId);
  }

  private void validateCreate(UserDto dto) {
    if (dto == null) {
      throw new ValidationException("User body must not be null.");
    }
    if (dto.getName() == null || dto.getName().isBlank()) {
      throw new ValidationException("User name must not be blank.");
    }
    if (dto.getEmail() == null || dto.getEmail().isBlank()) {
      throw new ValidationException("User email must not be blank.");
    }
  }
}
