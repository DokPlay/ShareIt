package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
/**
 * Captures a user's request for an item that may be fulfilled later.
 */
public class ItemRequest {
  private Long id;
  private String description;
  private User requestor;
  private LocalDateTime created;
}
