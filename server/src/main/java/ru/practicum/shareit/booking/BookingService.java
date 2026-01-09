package ru.practicum.shareit.booking;

import java.util.List;

/**
 * Service contract for booking operations.
 */
public interface BookingService {

  /**
   * Creates a new booking request.
   */
  BookingDto create(long userId, BookingCreateDto bookingCreateDto);

  /**
   * Approves or rejects a booking by item owner.
   */
  BookingDto approve(long userId, long bookingId, boolean approved);

  /**
   * Gets booking by id for authorized user (booker or item owner).
   */
  BookingDto getById(long userId, long bookingId);

  /**
   * Gets all bookings for current user filtered by state.
   */
  List<BookingDto> getAllByBooker(long userId, BookingState state);

  /**
   * Gets all bookings for items owned by user filtered by state.
   */
  List<BookingDto> getAllByOwner(long userId, BookingState state);
}
