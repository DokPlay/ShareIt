package ru.practicum.shareit.item;

import ru.practicum.shareit.user.User;

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

  /**
   * Builds a Comment domain object from DTO plus resolved associations.
   */
  public static Comment toComment(CommentDto dto, Item item, User author) {
    if (dto == null) {
      return null;
    }
    Comment comment = new Comment();
    comment.setId(dto.getId());
    comment.setText(dto.getText());
    comment.setItem(item);
    comment.setAuthor(author);
    comment.setCreated(dto.getCreated());
    return comment;
  }
}
