package ru.practicum.shareit.booking;

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
 * REST controller for booking operations.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

  private static final String USER_HEADER = "X-Sharer-User-Id";

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  /**
   * Creates a new booking request.
   */
  @PostMapping
  public BookingDto create(
      @RequestHeader(USER_HEADER) long userId,
      @RequestBody BookingCreateDto bookingCreateDto
  ) {
    return bookingService.create(userId, bookingCreateDto);
  }

  /**
   * Approves or rejects a booking request by item owner.
   */
  @PatchMapping("/{bookingId}")
  public BookingDto approve(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long bookingId,
      @RequestParam("approved") boolean approved
  ) {
    return bookingService.approve(userId, bookingId, approved);
  }

  /**
   * Gets booking details by id.
   */
  @GetMapping("/{bookingId}")
  public BookingDto getById(
      @RequestHeader(USER_HEADER) long userId,
      @PathVariable long bookingId
  ) {
    return bookingService.getById(userId, bookingId);
  }

  /**
   * Gets all bookings for current user filtered by state.
   */
  @GetMapping
  public List<BookingDto> getAllByBooker(
      @RequestHeader(USER_HEADER) long userId,
      @RequestParam(value = "state", defaultValue = "ALL") BookingState state,
      @RequestParam(value = "from", defaultValue = "0") int from,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    return bookingService.getAllByBooker(userId, state, from, size);
  }

  /**
   * Gets all bookings for items owned by current user filtered by state.
   */
  @GetMapping("/owner")
  public List<BookingDto> getAllByOwner(
      @RequestHeader(USER_HEADER) long userId,
      @RequestParam(value = "state", defaultValue = "ALL") BookingState state,
      @RequestParam(value = "from", defaultValue = "0") int from,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    return bookingService.getAllByOwner(userId, state, from, size);
  }
}
