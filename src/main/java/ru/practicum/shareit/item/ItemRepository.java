package ru.practicum.shareit.item;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for Item entities.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

  /**
   * Finds all items owned by a specific user, ordered by id.
   */
  List<Item> findByOwnerIdOrderByIdAsc(Long ownerId);

  /**
   * Searches available items by text in name or description (case-insensitive).
   */
  @Query("SELECT i FROM Item i WHERE i.available = true " +
         "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
         "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
  List<Item> searchAvailableByText(@Param("text") String text);
}
