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

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  /**
   * Converts missing entities to 404 responses.
   */
  public ErrorResponse handleNotFound(NotFoundException e) {
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  /**
   * Surfaces business conflicts as 409 responses.
   */
  public ErrorResponse handleConflict(ConflictException e) {
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  /**
   * Signals validation problems with 400 responses.
   */
  public ErrorResponse handleValidation(ValidationException e) {
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  /**
   * Signals annotation-based validation problems with 400 responses.
   */
  public ErrorResponse handleAnnotationValidation(MethodArgumentNotValidException e) {
    return new ErrorResponse("Validation error: " + e.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  /**
   * Handles invalid enum values in request parameters.
   */
  public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return new ErrorResponse("Unknown state: " + e.getValue());
  }

  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  /**
   * Catch-all fallback to protect clients from leaking stack traces.
   */
  public ErrorResponse handleOther(Throwable e) {
    return new ErrorResponse("Unexpected error: " + e.getMessage());
  }
}