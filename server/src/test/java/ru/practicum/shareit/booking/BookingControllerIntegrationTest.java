package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() throws Exception {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Create owner
        UserDto ownerDto = new UserDto(null, "Owner", "owner@email.com");
        String ownerResponse = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerDto)))
                .andReturn().getResponse().getContentAsString();
        ownerId = objectMapper.readValue(ownerResponse, UserDto.class).getId();

        // Create booker
        UserDto bookerDto = new UserDto(null, "Booker", "booker@email.com");
        String bookerResponse = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookerDto)))
                .andReturn().getResponse().getContentAsString();
        bookerId = objectMapper.readValue(bookerResponse, UserDto.class).getId();

        // Create item
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        String itemResponse = mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andReturn().getResponse().getContentAsString();
        itemId = objectMapper.readValue(itemResponse, ItemDto.class).getId();
    }

    @Test
    void createBooking_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.item.id", is(itemId.intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookerId.intValue())));
    }

    @Test
    void createBooking_ItemNotFound_Returns404() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            999L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_OwnerCannotBook_Returns404() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBooking_Approve_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        String response = mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long bookingId = objectMapper.readValue(response, BookingDto.class).getId();

        mockMvc.perform(patch("/bookings/" + bookingId)
                .header(USER_HEADER, ownerId)
                .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveBooking_Reject_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        String response = mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long bookingId = objectMapper.readValue(response, BookingDto.class).getId();

        mockMvc.perform(patch("/bookings/" + bookingId)
                .header(USER_HEADER, ownerId)
                .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void approveBooking_NotOwner_Returns404() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        String response = mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long bookingId = objectMapper.readValue(response, BookingDto.class).getId();

        mockMvc.perform(patch("/bookings/" + bookingId)
                .header(USER_HEADER, bookerId)
                .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooking_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        String response = mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long bookingId = objectMapper.readValue(response, BookingDto.class).getId();

        mockMvc.perform(get("/bookings/" + bookingId)
                .header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId.intValue())));
    }

    @Test
    void getAllByBooker_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/bookings")
                .header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllByBooker_WithState_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/bookings")
                .header(USER_HEADER, bookerId)
                .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllByBooker_InvalidState_Returns400() throws Exception {
        mockMvc.perform(get("/bookings")
                .header(USER_HEADER, bookerId)
                .param("state", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("INVALID")));
    }

    @Test
    void getAllByOwner_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/bookings/owner")
                .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllByOwner_WithState_Success() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
            itemId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, bookerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/bookings/owner")
                .header(USER_HEADER, ownerId)
                .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
