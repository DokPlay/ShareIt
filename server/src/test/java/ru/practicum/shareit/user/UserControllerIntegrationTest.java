package ru.practicum.shareit.user;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_Success() throws Exception {
        UserDto userDto = new UserDto(null, "Test User", "test@email.com");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@email.com")));
    }

    @Test
    void createUser_DuplicateEmail_Returns409() throws Exception {
        UserDto user1 = new UserDto(null, "User 1", "same@email.com");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        UserDto user2 = new UserDto(null, "User 2", "same@email.com");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_Success() throws Exception {
        UserDto userDto = new UserDto(null, "Original", "original@email.com");
        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn().getResponse().getContentAsString();
        
        UserDto created = objectMapper.readValue(response, UserDto.class);

        UserDto updateDto = new UserDto(null, "Updated", null);
        mockMvc.perform(patch("/users/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.email", is("original@email.com")));
    }

    @Test
    void getUser_Success() throws Exception {
        UserDto userDto = new UserDto(null, "Test User", "test@email.com");
        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn().getResponse().getContentAsString();
        
        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(get("/users/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    void getUser_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_Success() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserDto(null, "User 1", "user1@email.com"))));
        
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserDto(null, "User 2", "user2@email.com"))));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deleteUser_Success() throws Exception {
        UserDto userDto = new UserDto(null, "Test User", "test@email.com");
        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn().getResponse().getContentAsString();
        
        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(delete("/users/" + created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + created.getId()))
                .andExpect(status().isNotFound());
    }
}
