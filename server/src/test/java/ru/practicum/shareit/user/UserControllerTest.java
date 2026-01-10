package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_Success() throws Exception {
        UserDto inputDto = new UserDto(null, "Test User", "test@email.com");
        UserDto outputDto = new UserDto(1L, "Test User", "test@email.com");

        when(userService.create(any(UserDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@email.com"));

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void createUser_DuplicateEmail_Returns409() throws Exception {
        UserDto inputDto = new UserDto(null, "Test User", "test@email.com");

        when(userService.create(any(UserDto.class)))
                .thenThrow(new ConflictException("Email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUser_Success() throws Exception {
        UserDto outputDto = new UserDto(1L, "Test User", "test@email.com");

        when(userService.getById(1L)).thenReturn(outputDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userService).getById(1L);
    }

    @Test
    void getUser_NotFound_Returns404() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_Success() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(1L, "User 1", "user1@email.com"),
                new UserDto(2L, "User 2", "user2@email.com")
        );

        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService).getAll();
    }

    @Test
    void updateUser_Success() throws Exception {
        UserDto inputDto = new UserDto(null, "Updated Name", null);
        UserDto outputDto = new UserDto(1L, "Updated Name", "test@email.com");

        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(outputDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(userService).update(eq(1L), any(UserDto.class));
    }

    @Test
    void updateUser_NotFound_Returns404() throws Exception {
        UserDto inputDto = new UserDto(null, "Updated Name", null);

        when(userService.update(anyLong(), any(UserDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }

    @Test
    void deleteUser_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("User not found")).when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }
}
