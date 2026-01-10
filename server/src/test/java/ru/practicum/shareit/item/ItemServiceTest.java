package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingCreateDto;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private UserDto owner;
    private UserDto booker;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userService.create(new UserDto(null, "Owner", "owner@email.com"));
        booker = userService.create(new UserDto(null, "Booker", "booker@email.com"));
    }

    @Test
    void createItem_Success() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);

        ItemDto created = itemService.create(owner.getId(), itemDto);

        assertNotNull(created.getId());
        assertEquals("Test Item", created.getName());
        assertEquals("Test Description", created.getDescription());
        assertTrue(created.getAvailable());
    }

    @Test
    void createItem_UserNotFound_ThrowsNotFound() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);

        assertThrows(NotFoundException.class, () -> itemService.create(999L, itemDto));
    }

    @Test
    void createItem_BlankName_ThrowsValidation() {
        ItemDto itemDto = new ItemDto(null, "", "Test Description", true, null);

        assertThrows(ValidationException.class, () -> itemService.create(owner.getId(), itemDto));
    }

    @Test
    void createItem_BlankDescription_ThrowsValidation() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "", true, null);

        assertThrows(ValidationException.class, () -> itemService.create(owner.getId(), itemDto));
    }

    @Test
    void createItem_NullAvailable_ThrowsValidation() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", null, null);

        assertThrows(ValidationException.class, () -> itemService.create(owner.getId(), itemDto));
    }

    @Test
    void updateItem_Success() {
        ItemDto itemDto = new ItemDto(null, "Original", "Original Desc", true, null);
        ItemDto created = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto(null, "Updated", "Updated Desc", false, null);
        ItemDto updated = itemService.update(owner.getId(), created.getId(), updateDto);

        assertEquals("Updated", updated.getName());
        assertEquals("Updated Desc", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void updateItem_PartialUpdate_Success() {
        ItemDto itemDto = new ItemDto(null, "Original", "Original Desc", true, null);
        ItemDto created = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto(null, "Updated Name", null, null, null);
        ItemDto updated = itemService.update(owner.getId(), created.getId(), updateDto);

        assertEquals("Updated Name", updated.getName());
        assertEquals("Original Desc", updated.getDescription());
        assertTrue(updated.getAvailable());
    }

    @Test
    void updateItem_NotOwner_ThrowsNotFound() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        ItemDto created = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto(null, "Updated", "Updated Desc", false, null);

        assertThrows(NotFoundException.class, () -> 
            itemService.update(booker.getId(), created.getId(), updateDto));
    }

    @Test
    void getById_Success() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        ItemDto created = itemService.create(owner.getId(), itemDto);

        ItemDto found = itemService.getById(owner.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Test Item", found.getName());
        assertNotNull(found.getComments());
    }

    @Test
    void getById_NotFound_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getById(owner.getId(), 999L));
    }

    @Test
    void getOwnerItems_Success() {
        itemService.create(owner.getId(), new ItemDto(null, "Item 1", "Desc 1", true, null));
        itemService.create(owner.getId(), new ItemDto(null, "Item 2", "Desc 2", true, null));

        List<ItemDto> items = itemService.getOwnerItems(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void getOwnerItems_Empty() {
        List<ItemDto> items = itemService.getOwnerItems(owner.getId());

        assertTrue(items.isEmpty());
    }

    @Test
    void search_Success() {
        itemService.create(owner.getId(), new ItemDto(null, "Дрель", "Простая дрель", true, null));
        itemService.create(owner.getId(), new ItemDto(null, "Отвертка", "Отвертка аккумуляторная", true, null));

        List<ItemDto> found = itemService.search(booker.getId(), "дрель");

        assertEquals(1, found.size());
        assertEquals("Дрель", found.get(0).getName());
    }

    @Test
    void search_NotAvailable_NotReturned() {
        itemService.create(owner.getId(), new ItemDto(null, "Дрель", "Простая дрель", false, null));

        List<ItemDto> found = itemService.search(booker.getId(), "дрель");

        assertTrue(found.isEmpty());
    }

    @Test
    void search_BlankText_ReturnsEmpty() {
        itemService.create(owner.getId(), new ItemDto(null, "Дрель", "Простая дрель", true, null));

        List<ItemDto> found = itemService.search(booker.getId(), "");

        assertTrue(found.isEmpty());
    }

    @Test
    void addComment_Success() throws InterruptedException {
        ItemDto item = itemService.create(owner.getId(), 
            new ItemDto(null, "Test Item", "Test Description", true, null));

        // Create a booking in the past
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusSeconds(1),
            LocalDateTime.now().plusSeconds(2)
        );
        BookingDto booking = bookingService.create(booker.getId(), bookingDto);
        bookingService.approve(owner.getId(), booking.getId(), true);

        // Wait for booking to complete
        Thread.sleep(3000);

        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);
        CommentDto created = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(created.getId());
        assertEquals("Great item!", created.getText());
        assertEquals("Booker", created.getAuthorName());
        assertNotNull(created.getCreated());
    }

    @Test
    void addComment_NoBooking_ThrowsValidation() {
        ItemDto item = itemService.create(owner.getId(), 
            new ItemDto(null, "Test Item", "Test Description", true, null));

        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);

        assertThrows(ValidationException.class, () -> 
            itemService.addComment(booker.getId(), item.getId(), commentDto));
    }

    @Test
    void addComment_BlankText_ThrowsValidation() throws InterruptedException {
        ItemDto item = itemService.create(owner.getId(), 
            new ItemDto(null, "Test Item", "Test Description", true, null));

        // Create a booking in the past
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusSeconds(1),
            LocalDateTime.now().plusSeconds(2)
        );
        BookingDto booking = bookingService.create(booker.getId(), bookingDto);
        bookingService.approve(owner.getId(), booking.getId(), true);

        Thread.sleep(3000);

        CommentDto commentDto = new CommentDto(null, "", null, null);

        assertThrows(ValidationException.class, () -> 
            itemService.addComment(booker.getId(), item.getId(), commentDto));
    }
}
