package ru.practicum.shareit.user;

/**
 * Converts between user domain entities and DTOs.
 */
public final class UserMapper {

  private UserMapper() {
  }

  /**
   * Maps a domain user to DTO.
   */
  public static UserDto toUserDto(User user) {
    if (user == null) {
      return null;
    }
    return new UserDto(user.getId(), user.getName(), user.getEmail());
  }

  /**
   * Builds a domain user from incoming DTO.
   */
  public static User toUser(UserDto dto) {
    if (dto == null) {
      return null;
    }
    return new User(dto.getId(), dto.getName(), dto.getEmail());
  }
}
