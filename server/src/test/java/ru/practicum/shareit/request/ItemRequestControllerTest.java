package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_Status200() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), null);

        when(itemRequestService.createRequest(anyLong(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getUserRequests_Status200() throws Exception {
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), null);

        when(itemRequestService.getUserRequests(anyLong())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllRequests_Status200() throws Exception {
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), null);

        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getRequestById_Status200() throws Exception {
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), null);

        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(itemRequestService).getRequestById(1L, 1L);
    }

    @Test
    void createRequest_UserNotFound_Returns404() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);

        when(itemRequestService.createRequest(anyLong(), any()))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestById_NotFound_Returns404() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestById_WithItems_Success() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Drill", "Good drill", true, 1L)
        );
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), items);

        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Drill"));
    }

    @Test
    void getAllRequests_EmptyList_Success() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
