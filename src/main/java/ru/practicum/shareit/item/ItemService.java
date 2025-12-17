package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {

  ItemDto create(long ownerId, ItemDto itemDto);

  ItemDto update(long ownerId, long itemId, ItemDto itemDto);

  ItemDto getById(long userId, long itemId);

  List<ItemDto> getOwnerItems(long ownerId);

  List<ItemDto> search(long userId, String text);
}
