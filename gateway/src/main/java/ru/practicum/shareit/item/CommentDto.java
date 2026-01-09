package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
  private Long id;

  @NotBlank(message = "Comment text must not be blank.")
  @Size(max = 2000, message = "Comment text must not exceed 2000 characters.")
  private String text;

  private String authorName;
  private LocalDateTime created;
}
