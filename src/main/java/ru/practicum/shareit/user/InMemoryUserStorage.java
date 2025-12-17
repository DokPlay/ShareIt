package ru.practicum.shareit.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

@Repository
public class InMemoryUserStorage implements UserStorage {

  private final Map<Long, User> users = new HashMap<>();
  private final AtomicLong idSeq = new AtomicLong(0);

  @Override
  public User create(User user) {
    long id = idSeq.incrementAndGet();
    user.setId(id);

    validateEmailUnique(user.getEmail(), id);
    users.put(id, copyOf(user));
    return copyOf(user);
  }

  @Override
  public User update(User user) {
    Long id = user.getId();
    if (id == null) {
      throw new NotFoundException("User id must be provided for update.");
    }
    if (!users.containsKey(id)) {
      throw new NotFoundException("User with id=" + id + " not found.");
    }

    validateEmailUnique(user.getEmail(), id);
    users.put(id, copyOf(user));
    return copyOf(user);
  }

  @Override
  public User getById(long userId) {
    User user = users.get(userId);
    if (user == null) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    return copyOf(user);
  }

  @Override
  public List<User> getAll() {
    List<User> result = new ArrayList<>(users.values());
    result.sort(Comparator.comparing(User::getId));
    return result.stream().map(this::copyOf).toList();
  }

  @Override
  public void delete(long userId) {
    if (!users.containsKey(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    users.remove(userId);
  }

  @Override
  public boolean existsById(long userId) {
    return users.containsKey(userId);
  }

  private void validateEmailUnique(String email, long currentUserId) {
    if (email == null) {
      return;
    }
    String normalized = email.trim().toLowerCase();
    for (User existing : users.values()) {
      if (!existing.getId().equals(currentUserId)
          && existing.getEmail() != null
          && existing.getEmail().trim().toLowerCase().equals(normalized)) {
        throw new ConflictException("Email '" + email + "' is already used by another user.");
      }
    }
  }

  private User copyOf(User user) {
    return new User(user.getId(), user.getName(), user.getEmail());
  }
}
