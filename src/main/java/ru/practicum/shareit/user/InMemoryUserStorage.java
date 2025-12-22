package ru.practicum.shareit.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

@Repository
public class InMemoryUserStorage implements UserStorage {

  private final Map<Long, User> users = new HashMap<>();
  private final Set<String> uniqueEmails = new HashSet<>();
  private final Map<Long, String> userEmails = new HashMap<>();
  private long idSeq = 0;

  @Override
  public User create(User user) {
    String email = user.getEmail();
    String normalizedEmail = null;

    if (email != null) {
      normalizedEmail = email.trim().toLowerCase();
      if (uniqueEmails.contains(normalizedEmail)) {
        throw new ConflictException("Email '" + email + "' is already used by another user.");
      }
    }

    user.setId(++idSeq);

    if (normalizedEmail != null) {
      uniqueEmails.add(normalizedEmail);
      userEmails.put(user.getId(), normalizedEmail);
    }

    users.put(user.getId(), user);
    return user;
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

    String newEmail = user.getEmail();
    if (newEmail != null) {
      String newNormalized = newEmail.trim().toLowerCase();
      String oldNormalized = userEmails.get(id);

      if (!newNormalized.equals(oldNormalized)) {
        if (uniqueEmails.contains(newNormalized)) {
          throw new ConflictException("Email '" + newEmail + "' is already used by another user.");
        }
        if (oldNormalized != null) {
          uniqueEmails.remove(oldNormalized);
        }
        uniqueEmails.add(newNormalized);
        userEmails.put(id, newNormalized);
      }
    }

    users.put(id, user);
    return user;
  }

  @Override
  public User getById(long userId) {
    User user = users.get(userId);
    if (user == null) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    return user;
  }

  @Override
  public List<User> getAll() {
    List<User> result = new ArrayList<>(users.values());
    result.sort(Comparator.comparing(User::getId));
    return result;
  }

  @Override
  public void delete(long userId) {
    if (!users.containsKey(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }

    String email = userEmails.get(userId);
    if (email != null) {
      uniqueEmails.remove(email);
      userEmails.remove(userId);
    }
    users.remove(userId);
  }

  @Override
  public boolean existsById(long userId) {
    return users.containsKey(userId);
  }
}
