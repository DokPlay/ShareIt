package ru.practicum.shareit.user;

public final class UserMapper {

  private UserMapper() {
  }

  public static UserDto toUserDto(User user) {
    if (user == null) {
      return null;
    }
    return new UserDto(user.getId(), user.getName(), user.getEmail());
  }

  public static User toUser(UserDto dto) {
    if (dto == null) {
      return null;
    }
    return new User(dto.getId(), dto.getName(), dto.getEmail());
  }
}
