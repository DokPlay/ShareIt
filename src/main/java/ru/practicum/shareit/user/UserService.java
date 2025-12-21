package ru.practicum.shareit.user;

import java.util.List;

/**
 * Service contract for user lifecycle operations.
 */
public interface UserService {

  UserDto create(UserDto userDto);

  UserDto update(long userId, UserDto userDto);

  UserDto getById(long userId);

  List<UserDto> getAll();

  void delete(long userId);
}
