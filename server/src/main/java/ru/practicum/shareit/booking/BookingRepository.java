package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for Booking entities.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

  // ========== Bookings by Booker ==========

  /**
   * Finds all bookings by booker, ordered by start date descending.
   */
       Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

  /**
   * Finds current bookings for a booker (start <= now < end).
   */
  @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
         "AND b.start <= :now AND b.end > :now")
  Page<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                      @Param("now") LocalDateTime now,
                                      Pageable pageable);

  /**
   * Finds past bookings for a booker (end < now).
   */
  @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
         "AND b.end < :now")
  Page<Booking> findPastByBookerId(@Param("bookerId") Long bookerId,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

  /**
   * Finds future bookings for a booker (start > now).
   */
  @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
         "AND b.start > :now")
  Page<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId,
                                     @Param("now") LocalDateTime now,
                                     Pageable pageable);

  /**
   * Finds bookings by booker with specific status.
   */
       Page<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

  // ========== Bookings by Owner ==========

  /**
   * Finds all bookings for items owned by a user.
   */
       @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
       Page<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

  /**
   * Finds current bookings for items owned by a user.
   */
  @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
         "AND b.start <= :now AND b.end > :now")
  Page<Booking> findCurrentByItemOwnerId(@Param("ownerId") Long ownerId,
                                         @Param("now") LocalDateTime now,
                                         Pageable pageable);

  /**
   * Finds past bookings for items owned by a user.
   */
  @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
         "AND b.end < :now")
  Page<Booking> findPastByItemOwnerId(@Param("ownerId") Long ownerId,
                                      @Param("now") LocalDateTime now,
                                      Pageable pageable);

  /**
   * Finds future bookings for items owned by a user.
   */
  @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
         "AND b.start > :now")
  Page<Booking> findFutureByItemOwnerId(@Param("ownerId") Long ownerId,
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);

  /**
   * Finds bookings for items owned by a user with specific status.
   */
  @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
         "AND b.status = :status")
  Page<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                           @Param("status") BookingStatus status,
                                           Pageable pageable);

  // ========== Bookings for Item ==========

  /**
   * Finds the last booking for an item (end < now, sorted by end desc).
   */
  @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
         "AND b.end < :now AND b.status = 'APPROVED' ORDER BY b.end DESC")
  List<Booking> findLastBookingByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

  /**
   * Finds the next booking for an item (start > now, sorted by start asc).
   */
  @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
         "AND b.start > :now AND b.status = 'APPROVED' ORDER BY b.start ASC")
  List<Booking> findNextBookingByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

  /**
   * Finds all bookings for items in the given list.
   */
  @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = 'APPROVED'")
  List<Booking> findByItemIdIn(@Param("itemIds") List<Long> itemIds);

  /**
   * Checks if user has completed a booking for an item.
   */
  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Booking b " +
         "WHERE b.item.id = :itemId AND b.booker.id = :bookerId " +
         "AND b.status = 'APPROVED' AND b.end < :now")
  boolean existsCompletedBooking(@Param("itemId") Long itemId, 
                                  @Param("bookerId") Long bookerId, 
                                  @Param("now") LocalDateTime now);

  /**
   * Checks for overlapping bookings.
   */
  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Booking b " +
         "WHERE b.item.id = :itemId AND b.status != 'REJECTED' " +
         "AND ((b.start < :end AND b.end > :start))")
  boolean existsOverlappingBooking(@Param("itemId") Long itemId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
}
