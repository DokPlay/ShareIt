package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

/**
 * Application-layer service encapsulating item validation and ownership checks.
 */
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final BookingRepository bookingRepository;
  private final CommentRepository commentRepository;

  public ItemServiceImpl(ItemRepository itemRepository,
                         UserRepository userRepository,
                         BookingRepository bookingRepository,
                         CommentRepository commentRepository) {
    this.itemRepository = itemRepository;
    this.userRepository = userRepository;
    this.bookingRepository = bookingRepository;
    this.commentRepository = commentRepository;
  }

  @Override
  @Transactional
  public ItemDto create(long ownerId, ItemDto itemDto) {
    validateCreate(itemDto);
    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new NotFoundException("User with id=" + ownerId + " not found."));

    ItemRequest request = null;
    if (itemDto.getRequestId() != null) {
      request = new ItemRequest();
      request.setId(itemDto.getRequestId());
    }

    Item item = ItemMapper.toItem(itemDto, owner, request);
    Item created = itemRepository.save(item);
    return ItemMapper.toItemDto(created);
  }

  @Override
  @Transactional
  public ItemDto update(long ownerId, long itemId, ItemDto itemDto) {
    if (!userRepository.existsById(ownerId)) {
      throw new NotFoundException("User with id=" + ownerId + " not found.");
    }

    Item existing = itemRepository.findById(itemId)
        .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found."));

    if (existing.getOwner() == null || existing.getOwner().getId() == null
        || !existing.getOwner().getId().equals(ownerId)) {
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

    Item updated = itemRepository.save(existing);
    return ItemMapper.toItemDto(updated);
  }

  @Override
  public ItemDto getById(long userId, long itemId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }

    Item item = itemRepository.findById(itemId)
        .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found."));

    ItemDto dto = ItemMapper.toItemDto(item);

    // Add comments
    List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
    dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());

    // Add booking info only for owner
    if (item.getOwner().getId().equals(userId)) {
      LocalDateTime now = LocalDateTime.now();
      List<Booking> lastBookings = bookingRepository.findLastBookingByItemId(itemId, now);
      if (!lastBookings.isEmpty()) {
        dto.setLastBooking(BookingMapper.toBookingShortDto(lastBookings.get(0)));
      }
      List<Booking> nextBookings = bookingRepository.findNextBookingByItemId(itemId, now);
      if (!nextBookings.isEmpty()) {
        dto.setNextBooking(BookingMapper.toBookingShortDto(nextBookings.get(0)));
      }
    }

    return dto;
  }

  @Override
  public List<ItemDto> getOwnerItems(long ownerId) {
    if (!userRepository.existsById(ownerId)) {
      throw new NotFoundException("User with id=" + ownerId + " not found.");
    }

    List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(ownerId);
    if (items.isEmpty()) {
      return new ArrayList<>();
    }

    List<Long> itemIds = items.stream().map(Item::getId).toList();
    LocalDateTime now = LocalDateTime.now();

    // Fetch all bookings for these items
    List<Booking> allBookings = bookingRepository.findByItemIdIn(itemIds);
    Map<Long, List<Booking>> bookingsByItem = allBookings.stream()
        .collect(Collectors.groupingBy(b -> b.getItem().getId()));

    // Fetch all comments for these items
    List<Comment> allComments = commentRepository.findByItemIdIn(itemIds);
    Map<Long, List<Comment>> commentsByItem = allComments.stream()
        .collect(Collectors.groupingBy(c -> c.getItem().getId()));

    return items.stream().map(item -> {
      ItemDto dto = ItemMapper.toItemDto(item);

      // Add comments
      List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), new ArrayList<>());
      dto.setComments(itemComments.stream().map(CommentMapper::toCommentDto).toList());

      // Add booking info
      List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), new ArrayList<>());

      // Find last booking (end < now or start < now, sorted by end desc)
      itemBookings.stream()
          .filter(b -> b.getStart().isBefore(now))
          .max((b1, b2) -> b1.getEnd().compareTo(b2.getEnd()))
          .ifPresent(b -> dto.setLastBooking(BookingMapper.toBookingShortDto(b)));

      // Find next booking (start > now, sorted by start asc)
      itemBookings.stream()
          .filter(b -> b.getStart().isAfter(now))
          .min((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
          .ifPresent(b -> dto.setNextBooking(BookingMapper.toBookingShortDto(b)));

      return dto;
    }).toList();
  }

  @Override
  public List<ItemDto> search(long userId, String text) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }
    if (text == null || text.isBlank()) {
      return new ArrayList<>();
    }
    return itemRepository.searchAvailableByText(text).stream()
        .map(ItemMapper::toItemDto)
        .toList();
  }

  @Override
  @Transactional
  public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
    User author = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

    Item item = itemRepository.findById(itemId)
        .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found."));

    // Check if user has completed a booking for this item
    boolean hasCompletedBooking = bookingRepository.existsCompletedBooking(itemId, userId, LocalDateTime.now());
    if (!hasCompletedBooking) {
      throw new ValidationException("User with id=" + userId + " has not completed a booking for item id=" + itemId + ".");
    }

    if (commentDto.getText() == null || commentDto.getText().isBlank()) {
      throw new ValidationException("Comment text must not be blank.");
    }

    Comment comment = new Comment();
    comment.setText(commentDto.getText());
    comment.setItem(item);
    comment.setAuthor(author);
    comment.setCreated(LocalDateTime.now());

    Comment saved = commentRepository.save(comment);
    return CommentMapper.toCommentDto(saved);
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
