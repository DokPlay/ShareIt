package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_Success() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        ItemDto outputDto = new ItemDto(1L, "Test Item", "Test Description", true, null);
        outputDto.setOwnerId(1L);

        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void createItem_UserNotFound_Returns404() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Test Item", "Test Description", true, null);

        when(itemService.create(anyLong(), any(ItemDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItem_Success() throws Exception {
        ItemDto outputDto = new ItemDto(1L, "Test Item", "Test Description", true, null);
        outputDto.setOwnerId(1L);
        outputDto.setComments(new ArrayList<>());

        when(itemService.getById(1L, 1L)).thenReturn(outputDto);

        mockMvc.perform(get("/items/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService).getById(1L, 1L);
    }

    @Test
    void getItem_NotFound_Returns404() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnerItems_Success() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Item 1", "Desc 1", true, null),
                new ItemDto(2L, "Item 2", "Desc 2", true, null)
        );

        when(itemService.getOwnerItems(1L)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(itemService).getOwnerItems(1L);
    }

    @Test
    void updateItem_Success() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Updated Name", null, null, null);
        ItemDto outputDto = new ItemDto(1L, "Updated Name", "Original Desc", true, null);

        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(outputDto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(itemService).update(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    void updateItem_NotOwner_Returns404() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Updated Name", null, null, null);

        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Item not found for owner"));

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItems_Success() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Drill", "Good drill", true, null)
        );

        when(itemService.search(anyLong(), eq("drill"))).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));

        verify(itemService).search(1L, "drill");
    }

    @Test
    void searchItems_EmptyText_ReturnsEmpty() throws Exception {
        when(itemService.search(anyLong(), eq(""))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, 1L)
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addComment_Success() throws Exception {
        CommentDto inputDto = new CommentDto(null, "Great item!", null, null);
        CommentDto outputDto = new CommentDto(1L, "Great item!", "John", LocalDateTime.now());

        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"));

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void addComment_NotCompletedBooking_Returns400() throws Exception {
        CommentDto inputDto = new CommentDto(null, "Great item!", null, null);

        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new ValidationException("No completed booking"));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }
}
