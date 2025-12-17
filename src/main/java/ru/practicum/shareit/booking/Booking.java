package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
/**
 * Domain model describing a reservation of an item.
 */
public class Booking {
  private Long id;
  private LocalDateTime start;
  private LocalDateTime end;
  private Item item;
  private User booker;
  private BookingStatus status;
}
