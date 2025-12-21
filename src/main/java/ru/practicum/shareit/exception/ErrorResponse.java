package ru.practicum.shareit.exception;

/**
 * Simple DTO returned to clients when an error occurs.
 */
public class ErrorResponse {
  private final String error;

  public ErrorResponse(String error) {
    this.error = error;
  }

  public String getError() {
    return error;
  }
}
