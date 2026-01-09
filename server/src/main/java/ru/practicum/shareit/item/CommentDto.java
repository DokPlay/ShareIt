package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO for transferring comment data via REST layer.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CommentDto {
  private Long id;

  @NotBlank(message = "Comment text must not be blank.")
  @Size(max = 2000, message = "Comment text must not exceed 2000 characters.")
  private String text;

  private String authorName;
  private LocalDateTime created;
}
