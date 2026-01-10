package ru.practicum.shareit.item;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.BookingShortDto;

/**
 * Transport-layer representation of an item used in HTTP requests and responses.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemDto {
  private Long id;
  private String name;
  private String description;

  private Boolean available;
  private Long requestId;
  private Long ownerId;
  private BookingShortDto lastBooking;
  private BookingShortDto nextBooking;
  private List<CommentDto> comments;

  public ItemDto(Long id, String name, String description, Boolean available, Long requestId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.available = available;
    this.requestId = requestId;
  }
}
