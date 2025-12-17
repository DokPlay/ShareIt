package ru.practicum.shareit.item;

import java.util.List;

public interface ItemStorage {

  Item create(Item item);

  Item update(Item item);

  Item getById(long itemId);

  List<Item> findByOwnerId(long ownerId);

  List<Item> searchAvailableByText(String text);

  boolean existsById(long itemId);
}
