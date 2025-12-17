package ru.practicum.shareit.item;

import java.util.List;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

@Service
public class ItemServiceImpl implements ItemService {

  private final ItemStorage itemStorage;
  private final UserStorage userStorage;

  public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
    this.itemStorage = itemStorage;
    this.userStorage = userStorage;
  }

  @Override
  public ItemDto create(long ownerId, ItemDto itemDto) {
    validateCreate(itemDto);
    User owner = userStorage.getById(ownerId);

    ItemRequest request = null;
    if (itemDto.getRequestId() != null) {
      request = new ItemRequest(itemDto.getRequestId(), null, null, null);
    }

    Item item = ItemMapper.toItem(itemDto, owner, request);
    Item created = itemStorage.create(item);
    return ItemMapper.toItemDto(created);
  }

  @Override
  public ItemDto update(long ownerId, long itemId, ItemDto itemDto) {
    if (!userStorage.existsById(ownerId)) {
      throw new NotFoundException("User with id=" + ownerId + " not found.");
    }

    Item existing = itemStorage.getById(itemId);
    if (existing.getOwner() == null || existing.getOwner().getId() == null
        || existing.getOwner().getId() != ownerId) {
      throw new NotFoundException("Item with id=" + itemId + " not found for owner id=" + ownerId + ".");
    }

    if (itemDto.getName() != null) {
      existing.setName(itemDto.getName());
    }
    if (itemDto.getDescription() != null) {
      existing.setDescription(itemDto.getDescription());
    }
    if (itemDto.getAvailable() != null) {
      existing.setAvailable(itemDto.getAvailable());
    }
    if (itemDto.getRequestId() != null) {
      existing.setRequest(new ItemRequest(itemDto.getRequestId(), null, null, null));
    }

    Item updated = itemStorage.update(existing);
    return ItemMapper.toItemDto(updated);
  }

  @Override
  public ItemDto getById(long userId, long itemId) {
    if (!userStorage.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    return ItemMapper.toItemDto(itemStorage.getById(itemId));
  }

  @Override
  public List<ItemDto> getOwnerItems(long ownerId) {
    if (!userStorage.existsById(ownerId)) {
      throw new NotFoundException("User with id=" + ownerId + " not found.");
    }
    return itemStorage.findByOwnerId(ownerId).stream().map(ItemMapper::toItemDto).toList();
  }

  @Override
  public List<ItemDto> search(long userId, String text) {
    if (!userStorage.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    return itemStorage.searchAvailableByText(text).stream().map(ItemMapper::toItemDto).toList();
  }

  private void validateCreate(ItemDto dto) {
    if (dto == null) {
      throw new ValidationException("Item body must not be null.");
    }
    if (dto.getName() == null || dto.getName().isBlank()) {
      throw new ValidationException("Item name must not be blank.");
    }
    if (dto.getDescription() == null || dto.getDescription().isBlank()) {
      throw new ValidationException("Item description must not be blank.");
    }
    if (dto.getAvailable() == null) {
      throw new ValidationException("Item available must be provided.");
    }
  }
}
