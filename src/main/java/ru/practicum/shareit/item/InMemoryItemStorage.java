package ru.practicum.shareit.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;

/**
 * In-memory item repository used for early-stage development and tests.
 */
@Repository
public class InMemoryItemStorage implements ItemStorage {

  private final Map<Long, Item> items = new HashMap<>();
  private final AtomicLong idSeq = new AtomicLong(0);

  @Override
  public Item create(Item item) {
    long id = idSeq.incrementAndGet();
    item.setId(id);
    items.put(id, copyOf(item));
    return copyOf(item);
  }

  @Override
  public Item update(Item item) {
    Long id = item.getId();
    if (id == null) {
      throw new NotFoundException("Item id must be provided for update.");
    }
    if (!items.containsKey(id)) {
      throw new NotFoundException("Item with id=" + id + " not found.");
    }
    items.put(id, copyOf(item));
    return copyOf(item);
  }

  @Override
  public Item getById(long itemId) {
    Item item = items.get(itemId);
    if (item == null) {
      throw new NotFoundException("Item with id=" + itemId + " not found.");
    }
    return copyOf(item);
  }

  @Override
  public List<Item> findByOwnerId(long ownerId) {
    List<Item> result = new ArrayList<>();
    for (Item item : items.values()) {
      if (item.getOwner() != null && item.getOwner().getId() != null
          && item.getOwner().getId().equals(ownerId)) {
        result.add(copyOf(item));
      }
    }
    result.sort(Comparator.comparing(Item::getId));
    return result;
  }

  @Override
  public List<Item> searchAvailableByText(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }
    String needle = text.trim().toLowerCase();

    List<Item> result = new ArrayList<>();
    for (Item item : items.values()) {
      if (!item.isAvailable()) {
        continue;
      }
      String name = item.getName() == null ? "" : item.getName().toLowerCase();
      String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
      if (name.contains(needle) || description.contains(needle)) {
        result.add(copyOf(item));
      }
    }
    result.sort(Comparator.comparing(Item::getId));
    return result;
  }

  @Override
  public boolean existsById(long itemId) {
    return items.containsKey(itemId);
  }

  private Item copyOf(Item item) {
    return new Item(
        item.getId(),
        item.getName(),
        item.getDescription(),
        item.isAvailable(),
        item.getOwner(),
        item.getRequest()
    );
  }
}
