package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

/**
 * DTO for transferring booking data in API responses.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class BookingDto {
  private Long id;
  private LocalDateTime start;
  private LocalDateTime end;
  private ItemDto item;
  private UserDto booker;
  private BookingStatus status;
}
