package ru.practicum.shareit.user;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

/**
 * Service layer handling validation and interaction with user repository.
 */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public UserDto create(UserDto userDto) {
    validateCreate(userDto);
    checkEmailUniqueness(userDto.getEmail(), null);
    User created = userRepository.save(UserMapper.toUser(userDto));
    return UserMapper.toUserDto(created);
  }

  @Override
  @Transactional
  public UserDto update(long userId, UserDto userDto) {
    User existing = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

    if (userDto.getName() != null) {
      existing.setName(userDto.getName());
    }
    if (userDto.getEmail() != null) {
      checkEmailUniqueness(userDto.getEmail(), userId);
      existing.setEmail(userDto.getEmail());
    }

    User updated = userRepository.save(existing);
    return UserMapper.toUserDto(updated);
  }

  @Override
  public UserDto getById(long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));
    return UserMapper.toUserDto(user);
  }

  @Override
  public List<UserDto> getAll() {
    return userRepository.findAll().stream().map(UserMapper::toUserDto).toList();
  }

  @Override
  @Transactional
  public void delete(long userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    userRepository.deleteById(userId);
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

  private void checkEmailUniqueness(String email, Long excludeUserId) {
    userRepository.findByEmail(email).ifPresent(existingUser -> {
      if (!existingUser.getId().equals(excludeUserId)) {
        throw new ConflictException("Email '" + email + "' is already used by another user.");
      }
    });
  }
}
