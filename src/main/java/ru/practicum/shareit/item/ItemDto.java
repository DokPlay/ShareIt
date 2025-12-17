package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
/**
 * Transport-layer representation of an item used in HTTP requests and responses.
 */
public class ItemDto {
  private Long id;
  private String name;
  private String description;
  private Boolean available;
  private Long requestId;
}
