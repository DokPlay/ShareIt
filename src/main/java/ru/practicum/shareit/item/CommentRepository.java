package ru.practicum.shareit.item;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for Comment entities.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

  /**
   * Finds all comments for a specific item, ordered by creation date descending.
   */
  List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

  /**
   * Finds all comments for items in the given list.
   */
  List<Comment> findByItemIdIn(List<Long> itemIds);
}
