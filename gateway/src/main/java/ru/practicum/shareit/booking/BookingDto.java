package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
  private Long id;
  private LocalDateTime start;
  private LocalDateTime end;
  private ItemDto item;
  private UserDto booker;
  private BookingStatus status;
}
