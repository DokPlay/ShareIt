package ru.practicum.shareit.item;

/**
 * Converts between Comment domain entities and DTOs.
 */
public final class CommentMapper {

  private CommentMapper() {
  }

  /**
   * Maps domain Comment to DTO.
   */
  public static CommentDto toCommentDto(Comment comment) {
    if (comment == null) {
      return null;
    }
    String authorName = comment.getAuthor() != null ? comment.getAuthor().getName() : null;
    return new CommentDto(
        comment.getId(),
        comment.getText(),
        authorName,
        comment.getCreated()
    );
  }
}
