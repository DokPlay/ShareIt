package ru.practicum.shareit.exception;

/**
 * Indicates client-provided data failed service-level validation.
 */
public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}
