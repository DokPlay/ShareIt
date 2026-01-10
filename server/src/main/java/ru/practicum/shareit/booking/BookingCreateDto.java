package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO for creating a new booking request.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
  private Long itemId;

  private LocalDateTime start;

  private LocalDateTime end;
}
