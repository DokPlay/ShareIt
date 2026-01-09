package ru.practicum.shareit.item;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * Converts between item domain entities and transport DTOs.
 */
public final class ItemMapper {

  private ItemMapper() {
  }

  /**
   * Maps domain item to outward-facing DTO.
   */
  public static ItemDto toItemDto(Item item) {
    if (item == null) {
      return null;
    }
    Long requestId = null;
    if (item.getRequest() != null) {
      requestId = item.getRequest().getId();
    }
    ItemDto dto = new ItemDto(
        item.getId(),
        item.getName(),
        item.getDescription(),
        item.isAvailable(),
        requestId
    );
    dto.setOwnerId(item.getOwner().getId());
    return dto;
  }

  /**
   * Builds an item domain object from DTO plus resolved associations.
   */
  public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
    if (dto == null) {
      return null;
    }
    boolean available = dto.getAvailable() != null && dto.getAvailable();
    return new Item(dto.getId(), dto.getName(), dto.getDescription(), available, owner, request);
  }
}
