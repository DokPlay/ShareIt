package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

/**
 * Converts between Booking domain entities and DTOs.
 */
@UtilityClass
public class BookingMapper {

  /**
   * Maps domain Booking to full DTO.
   */
  public static BookingDto toBookingDto(Booking booking) {
    if (booking == null) {
      return null;
    }
    return new BookingDto(
        booking.getId(),
        booking.getStart(),
        booking.getEnd(),
        ItemMapper.toItemDto(booking.getItem()),
        UserMapper.toUserDto(booking.getBooker()),
        booking.getStatus()
    );
  }

  /**
   * Maps domain Booking to short DTO for item details.
   */
  public static BookingShortDto toBookingShortDto(Booking booking) {
    if (booking == null) {
      return null;
    }
    Long bookerId = booking.getBooker() != null ? booking.getBooker().getId() : null;
    return new BookingShortDto(
        booking.getId(),
        bookerId,
        booking.getStart(),
        booking.getEnd()
    );
  }

  /**
   * Builds a Booking domain object from create DTO plus resolved associations.
   */
  public static Booking toBooking(BookingCreateDto dto, Item item, User booker) {
    if (dto == null) {
      return null;
    }
    Booking booking = new Booking();
    booking.setStart(dto.getStart());
    booking.setEnd(dto.getEnd());
    booking.setItem(item);
    booking.setBooker(booker);
    booking.setStatus(BookingStatus.WAITING);
    return booking;
  }
}
