package ru.practicum.shareit.item;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
public class ItemController {

  private static final String USER_HEADER = "X-Sharer-User-Id";

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping
  public ItemDto create(
      @RequestHeader(USER_HEADER) long userId,
      @RequestBody ItemDto itemDto
  ) {
    return itemService.create(userId, itemDto);
  }

  @PatchMapping("/{itemId}")
  public ItemDto update(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long itemId,
      @RequestBody ItemDto itemDto
  ) {
    return itemService.update(userId, itemId, itemDto);
  }

  @GetMapping("/{itemId}")
  public ItemDto getById(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long itemId
  ) {
    return itemService.getById(userId, itemId);
  }

  @GetMapping
  public List<ItemDto> getOwnerItems(@RequestHeader(USER_HEADER) long userId) {
    return itemService.getOwnerItems(userId);
  }

  @GetMapping("/search")
  public List<ItemDto> search(
      @RequestHeader(USER_HEADER) long userId,
      @RequestParam("text") String text
  ) {
    return itemService.search(userId, text);
  }
}
