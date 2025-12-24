package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

/**
 * Service implementation for booking operations.
 */
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;
  private final ItemRepository itemRepository;

  public BookingServiceImpl(BookingRepository bookingRepository,
                            UserRepository userRepository,
                            ItemRepository itemRepository) {
    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
  }

  @Override
  @Transactional
  public BookingDto create(long userId, BookingCreateDto dto) {
    validateBookingCreate(dto);

    User booker = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

    Item item = itemRepository.findById(dto.getItemId())
        .orElseThrow(() -> new NotFoundException("Item with id=" + dto.getItemId() + " not found."));

    if (!item.isAvailable()) {
      throw new ValidationException("Item with id=" + dto.getItemId() + " is not available for booking.");
    }

    if (item.getOwner().getId().equals(userId)) {
      throw new NotFoundException("Owner cannot book their own item.");
    }

    Booking booking = BookingMapper.toBooking(dto, item, booker);
    Booking saved = bookingRepository.save(booking);
    return BookingMapper.toBookingDto(saved);
  }

  @Override
  @Transactional
  public BookingDto approve(long userId, long bookingId, boolean approved) {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new NotFoundException("Booking with id=" + bookingId + " not found."));

    if (!booking.getItem().getOwner().getId().equals(userId)) {
      throw new NotFoundException("User with id=" + userId + " is not the owner of the item.");
    }

    if (booking.getStatus() != BookingStatus.WAITING) {
      throw new ValidationException("Booking status is already set.");
    }

    booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
    Booking updated = bookingRepository.save(booking);
    return BookingMapper.toBookingDto(updated);
  }

  @Override
  public BookingDto getById(long userId, long bookingId) {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new NotFoundException("Booking with id=" + bookingId + " not found."));

    boolean isBooker = booking.getBooker().getId().equals(userId);
    boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

    if (!isBooker && !isOwner) {
      throw new NotFoundException("Booking with id=" + bookingId + " not found for user id=" + userId + ".");
    }

    return BookingMapper.toBookingDto(booking);
  }

  @Override
  public List<BookingDto> getAllByBooker(long userId, BookingState state) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }

    LocalDateTime now = LocalDateTime.now();
    List<Booking> bookings;

    switch (state) {
      case CURRENT:
        bookings = bookingRepository.findCurrentByBookerId(userId, now);
        break;
      case PAST:
        bookings = bookingRepository.findPastByBookerId(userId, now);
        break;
      case FUTURE:
        bookings = bookingRepository.findFutureByBookerId(userId, now);
        break;
      case WAITING:
        bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
        break;
      case REJECTED:
        bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        break;
      default:
        bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
    }

    return bookings.stream().map(BookingMapper::toBookingDto).toList();
  }

  @Override
  public List<BookingDto> getAllByOwner(long userId, BookingState state) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User with id=" + userId + " not found.");
    }

    LocalDateTime now = LocalDateTime.now();
    List<Booking> bookings;

    switch (state) {
      case CURRENT:
        bookings = bookingRepository.findCurrentByItemOwnerId(userId, now);
        break;
      case PAST:
        bookings = bookingRepository.findPastByItemOwnerId(userId, now);
        break;
      case FUTURE:
        bookings = bookingRepository.findFutureByItemOwnerId(userId, now);
        break;
      case WAITING:
        bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
        break;
      case REJECTED:
        bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        break;
      default:
        bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
    }

    return bookings.stream().map(BookingMapper::toBookingDto).toList();
  }

  private void validateBookingCreate(BookingCreateDto dto) {
    if (dto == null) {
      throw new ValidationException("Booking data must not be null.");
    }
    if (dto.getItemId() == null) {
      throw new ValidationException("Item id must be provided.");
    }
    if (dto.getStart() == null) {
      throw new ValidationException("Start date must be provided.");
    }
    if (dto.getEnd() == null) {
      throw new ValidationException("End date must be provided.");
    }
    if (dto.getStart().isBefore(LocalDateTime.now())) {
      throw new ValidationException("Start date must be in the future.");
    }
    if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
      throw new ValidationException("End date must be after start date.");
    }
  }
}
