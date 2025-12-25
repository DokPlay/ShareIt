package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Short DTO for booking info shown in item details.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingShortDto {
  private Long id;
  private Long bookerId;
  private LocalDateTime start;
  private LocalDateTime end;
}
