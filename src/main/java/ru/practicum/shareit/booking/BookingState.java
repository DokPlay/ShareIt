package ru.practicum.shareit.booking;

/**
 * States for filtering booking queries in API requests.
 */
public enum BookingState {
  ALL,
  CURRENT,
  PAST,
  FUTURE,
  WAITING,
  REJECTED
}
