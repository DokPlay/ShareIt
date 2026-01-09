package ru.practicum.shareit.item;

import jakarta.validation.constraints.Size;
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

  @Size(max = 255, message = "Item name must not exceed 255 characters.")
  private String name;

  @Size(max = 1000, message = "Item description must not exceed 1000 characters.")
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
