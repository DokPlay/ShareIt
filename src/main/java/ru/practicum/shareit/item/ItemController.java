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

/**
 * REST endpoints for CRUD operations over items.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

  private static final String USER_HEADER = "X-Sharer-User-Id";

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  /**
   * Registers a new item owned by the requester.
   */
  @PostMapping
  public ItemDto create(
      @RequestHeader(USER_HEADER) long userId,
      @RequestBody ItemDto itemDto
  ) {
    return itemService.create(userId, itemDto);
  }

  /**
   * Applies partial updates to an existing item when owned by the caller.
   */
  @PatchMapping("/{itemId}")
  public ItemDto update(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long itemId,
      @RequestBody ItemDto itemDto
  ) {
    return itemService.update(userId, itemId, itemDto);
  }

  /**
   * Retrieves item details considering requester visibility rules.
   */
  @GetMapping("/{itemId}")
  public ItemDto getById(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long itemId
  ) {
    return itemService.getById(userId, itemId);
  }

  /**
   * Lists all items belonging to the provided owner id.
   */
  @GetMapping
  public List<ItemDto> getOwnerItems(@RequestHeader(USER_HEADER) long userId) {
    return itemService.getOwnerItems(userId);
  }

  /**
   * Searches available items by text across name and description fields.
   */
  @GetMapping("/search")
  public List<ItemDto> search(
      @RequestHeader(USER_HEADER) long userId,
      @RequestParam("text") String text
  ) {
    return itemService.search(userId, text);
  }

  /**
   * Adds a comment to an item from a user who has completed a booking.
   */
  @PostMapping("/{itemId}/comment")
  public CommentDto addComment(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long itemId,
      @RequestBody CommentDto commentDto
  ) {
    return itemService.addComment(userId, itemId, commentDto);
  }
}
