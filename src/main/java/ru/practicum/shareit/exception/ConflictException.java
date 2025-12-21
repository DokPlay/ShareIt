package ru.practicum.shareit.exception;

/**
 * Signals a business rule collision such as duplicate email or entity ownership clash.
 */
public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
