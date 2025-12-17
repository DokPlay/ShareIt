package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {

  User create(User user);

  User update(User user);

  User getById(long userId);

  List<User> getAll();

  void delete(long userId);

  boolean existsById(long userId);
}
