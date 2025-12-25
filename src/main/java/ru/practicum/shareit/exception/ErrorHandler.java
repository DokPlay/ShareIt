package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized REST exception mapping to predictable HTTP responses.
 */
@RestControllerAdvice
public class ErrorHandler {

  /**
   * Converts missing entities to 404 responses.
   */
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(NotFoundException e) {
    return new ErrorResponse(e.getMessage());
  }

  /**
   * Surfaces business conflicts as 409 responses.
   */
  @ExceptionHandler(ConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleConflict(ConflictException e) {
    return new ErrorResponse(e.getMessage());
  }

  /**
   * Handles all validation and type mismatch errors with 400 responses.
   */
  @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(Exception e) {
    if (e instanceof MethodArgumentTypeMismatchException mismatch) {
      return new ErrorResponse("Unknown state: " + mismatch.getValue());
    }
    if (e instanceof MethodArgumentNotValidException) {
      return new ErrorResponse("Validation error: " + e.getMessage());
    }
    return new ErrorResponse(e.getMessage());
  }

  /**
   * Catch-all fallback to protect clients from leaking stack traces.
   */
  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleOther(Throwable e) {
    return new ErrorResponse("Unexpected error: " + e.getMessage());
  }
}