package ru.practicum.shareit.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by email address.
   */
  Optional<User> findByEmail(String email);

  /**
   * Checks if a user with given email already exists.
   */
  boolean existsByEmail(String email);
}
