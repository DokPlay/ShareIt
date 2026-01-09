package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO for transferring user data via REST layer.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserDto {
  private Long id;

  @Size(max = 255, message = "User name must not exceed 255 characters.")
  private String name;

  @Email
  @Size(max = 512, message = "User email must not exceed 512 characters.")
  private String email;
}