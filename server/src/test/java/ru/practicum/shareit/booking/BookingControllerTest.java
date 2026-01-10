package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void createBooking_Success() throws Exception {
        BookingCreateDto inputDto = new BookingCreateDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        ItemDto itemDto = new ItemDto(1L, "Test Item", "Desc", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        BookingDto outputDto = new BookingDto(
                1L,
                inputDto.getStart(),
                inputDto.getEnd(),
                itemDto,
                bookerDto,
                BookingStatus.WAITING
        );

        when(bookingService.create(anyLong(), any(BookingCreateDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).create(eq(2L), any(BookingCreateDto.class));
    }

    @Test
    void createBooking_ItemNotFound_Returns404() throws Exception {
        BookingCreateDto inputDto = new BookingCreateDto(
                999L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingService.create(anyLong(), any(BookingCreateDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_ItemNotAvailable_Returns400() throws Exception {
        BookingCreateDto inputDto = new BookingCreateDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingService.create(anyLong(), any(BookingCreateDto.class)))
                .thenThrow(new ValidationException("Item not available"));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_Success() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Desc", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        BookingDto outputDto = new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemDto,
                bookerDto,
                BookingStatus.APPROVED
        );

        when(bookingService.getById(2L, 1L)).thenReturn(outputDto);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).getById(2L, 1L);
    }

    @Test
    void getBooking_NotFound_Returns404() throws Exception {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBooking_Approve_Success() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Desc", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        BookingDto outputDto = new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemDto,
                bookerDto,
                BookingStatus.APPROVED
        );

        when(bookingService.approve(1L, 1L, true)).thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approve(1L, 1L, true);
    }

    @Test
    void approveBooking_Reject_Success() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Desc", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        BookingDto outputDto = new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemDto,
                bookerDto,
                BookingStatus.REJECTED
        );

        when(bookingService.approve(1L, 1L, false)).thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingService).approve(1L, 1L, false);
    }

    @Test
    void approveBooking_NotOwner_Returns404() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Not owner"));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 999L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBooking_AlreadyApproved_Returns400() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ValidationException("Already approved"));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByBooker_Success() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Desc", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        List<BookingDto> bookings = List.of(
                new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        itemDto, bookerDto, BookingStatus.WAITING)
        );

        when(bookingService.getAllByBooker(2L, BookingState.ALL, 0, 10)).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

                verify(bookingService).getAllByBooker(2L, BookingState.ALL, 0, 10);
    }

    @Test
    void getAllByBooker_WaitingState_Success() throws Exception {
                when(bookingService.getAllByBooker(2L, BookingState.WAITING, 0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

                verify(bookingService).getAllByBooker(2L, BookingState.WAITING, 0, 10);
    }

    @Test
    void getAllByOwner_Success() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Desc", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");
        List<BookingDto> bookings = List.of(
                new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        itemDto, bookerDto, BookingStatus.WAITING)
        );

        when(bookingService.getAllByOwner(1L, BookingState.ALL, 0, 10)).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

                verify(bookingService).getAllByOwner(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getAllByOwner_NoItems_Returns404() throws Exception {
        when(bookingService.getAllByOwner(anyLong(), any(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("No items"));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 999L))
                .andExpect(status().isNotFound());
    }
}
