package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userService.create(new UserDto(null, "Owner", "owner@email.com"));
        booker = userService.create(new UserDto(null, "Booker", "booker@email.com"));
        item = itemService.create(owner.getId(), 
            new ItemDto(null, "Test Item", "Test Description", true, null));
    }

    @Test
    void createBooking_Success() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        BookingDto created = bookingService.create(booker.getId(), dto);

        assertNotNull(created.getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(booker.getId(), created.getBooker().getId());
    }

    @Test
    void createBooking_ItemNotFound_ThrowsNotFound() {
        BookingCreateDto dto = new BookingCreateDto(
            999L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_UserNotFound_ThrowsNotFound() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(999L, dto));
    }

    @Test
    void createBooking_ItemNotAvailable_ThrowsValidation() {
        ItemDto unavailableItem = itemService.create(owner.getId(), 
            new ItemDto(null, "Unavailable", "Not available", false, null));

        BookingCreateDto dto = new BookingCreateDto(
            unavailableItem.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_OwnerCannotBookOwnItem_ThrowsNotFound() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), dto));
    }

    @Test
    void createBooking_StartInPast_ThrowsValidation() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1)
        );

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_EndBeforeStart_ThrowsValidation() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(2),
            LocalDateTime.now().plusDays(1)
        );

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_EndEqualsStart_ThrowsValidation() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        BookingCreateDto dto = new BookingCreateDto(item.getId(), time, time);

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void approveBooking_Approve_Success() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);

        BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approveBooking_Reject_Success() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);

        BookingDto rejected = bookingService.approve(owner.getId(), created.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void approveBooking_NotOwner_ThrowsNotFound() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);

        assertThrows(NotFoundException.class, () -> 
            bookingService.approve(booker.getId(), created.getId(), true));
    }

    @Test
    void approveBooking_AlreadyApproved_ThrowsValidation() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);
        bookingService.approve(owner.getId(), created.getId(), true);

        assertThrows(ValidationException.class, () -> 
            bookingService.approve(owner.getId(), created.getId(), true));
    }

    @Test
    void getById_AsBooker_Success() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);

        BookingDto found = bookingService.getById(booker.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getById_AsOwner_Success() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);

        BookingDto found = bookingService.getById(owner.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getById_NotParticipant_ThrowsNotFound() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingDto created = bookingService.create(booker.getId(), dto);

        UserDto other = userService.create(new UserDto(null, "Other", "other@email.com"));

        assertThrows(NotFoundException.class, () -> 
            bookingService.getById(other.getId(), created.getId()));
    }

    @Test
    void getAllByBooker_All_Success() {
        createTestBooking();
        createTestBooking();

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), BookingState.ALL, 0, 10);

        assertEquals(2, bookings.size());
    }

    @Test
    void getAllByBooker_Waiting_Success() {
        createTestBooking();

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), BookingState.WAITING, 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void getAllByBooker_Rejected_Success() {
        BookingDto booking = createTestBooking();
        bookingService.approve(owner.getId(), booking.getId(), false);

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), BookingState.REJECTED, 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());
    }

    @Test
    void getAllByBooker_Future_Success() {
        createTestBooking();

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE, 0, 10);

        assertEquals(1, bookings.size());
    }

    @Test
    void getAllByBooker_UserNotFound_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () ->
            bookingService.getAllByBooker(999L, BookingState.ALL, 0, 10));
    }

    @Test
    void getAllByOwner_All_Success() {
        createTestBooking();
        createTestBooking();

        List<BookingDto> bookings = bookingService.getAllByOwner(owner.getId(), BookingState.ALL, 0, 10);

        assertEquals(2, bookings.size());
    }

    @Test
    void getAllByOwner_Waiting_Success() {
        createTestBooking();

        List<BookingDto> bookings = bookingService.getAllByOwner(owner.getId(), BookingState.WAITING, 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void getAllByOwner_UserNotFound_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () ->
            bookingService.getAllByOwner(999L, BookingState.ALL, 0, 10));
    }

    private BookingDto createTestBooking() {
        BookingCreateDto dto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        return bookingService.create(booker.getId(), dto);
    }
}
