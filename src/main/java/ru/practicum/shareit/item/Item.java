package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
/**
 * Item aggregate with owner and optional request reference.
 */
public class Item {
  private Long id;
  private String name;
  private String description;
  private boolean available;
  private User owner;
  private ItemRequest request;
}
