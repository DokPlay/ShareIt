package ru.practicum.shareit.item;

import java.util.List;

/**
 * Contract for item operations exposed to controllers and other services.
 */
public interface ItemService {

  /**
   * Creates a new item for the specified owner.
   */
  ItemDto create(long ownerId, ItemDto itemDto);

  /**
   * Updates mutable fields of an existing item; ownership is validated externally.
   */
  ItemDto update(long ownerId, long itemId, ItemDto itemDto);

  /**
   * Returns item details for a requesting user, enabling visibility rules at service layer.
   */
  ItemDto getById(long userId, long itemId);

  /**
   * Lists all items belonging to a specific owner in a stable order defined by implementation.
   */
  List<ItemDto> getOwnerItems(long ownerId);

  /**
   * Performs text-based search across available items, typically ignoring unavailable entries.
   */
  List<ItemDto> search(long userId, String text);
}
