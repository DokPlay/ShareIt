package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
  @NotNull(message = "Item id must be provided.")
  private Long itemId;

  @NotNull(message = "Start date must be provided.")
  @FutureOrPresent(message = "Start date must be in the future.")
  private LocalDateTime start;

  @NotNull(message = "End date must be provided.")
  @Future(message = "End date must be in the future.")
  private LocalDateTime end;
}
