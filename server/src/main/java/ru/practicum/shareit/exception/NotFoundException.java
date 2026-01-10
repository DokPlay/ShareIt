package ru.practicum.shareit.exception;

/**
 * Thrown when requested entity is absent in the storage layer.
 */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
