package ru.practicum.shareit.item;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

public final class ItemMapper {

  private ItemMapper() {
  }

  public static ItemDto toItemDto(Item item) {
    if (item == null) {
      return null;
    }
    Long requestId = null;
    if (item.getRequest() != null) {
      requestId = item.getRequest().getId();
    }
    return new ItemDto(
        item.getId(),
        item.getName(),
        item.getDescription(),
        item.isAvailable(),
        requestId
    );
  }

  public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
    if (dto == null) {
      return null;
    }
    boolean available = dto.getAvailable() != null && dto.getAvailable();
    return new Item(dto.getId(), dto.getName(), dto.getDescription(), available, owner, request);
  }
}
