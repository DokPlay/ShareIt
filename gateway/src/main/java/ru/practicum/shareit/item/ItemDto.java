package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingShortDto;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
  private Long id;

  @NotBlank(groups = Create.class, message = "Item name must not be blank.")
  @Size(max = 255, groups = {Create.class, Update.class})
  private String name;

  @NotBlank(groups = Create.class, message = "Item description must not be blank.")
  @Size(max = 1000, groups = {Create.class, Update.class})
  private String description;

  @NotNull(groups = Create.class, message = "Item available status must not be null.")
  private Boolean available;
  
  private Long requestId;
  private Long ownerId;
  private BookingShortDto lastBooking;
  private BookingShortDto nextBooking;
  private List<CommentDto> comments;
}
