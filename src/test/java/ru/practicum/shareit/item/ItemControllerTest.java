package ru.practicum.shareit.item;

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
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private Long ownerId;

    @BeforeEach
    void setUp() throws Exception {
        itemRepository.deleteAll();
        userRepository.deleteAll();

        UserDto userDto = new UserDto(null, "Owner", "owner@email.com");
        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn().getResponse().getContentAsString();
        
        UserDto created = objectMapper.readValue(response, UserDto.class);
        ownerId = created.getId();
    }

    @Test
    void createItem_Success() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createItem_UserNotFound_Returns404() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                .header(USER_HEADER, 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItem_BlankName_Returns400() throws Exception {
        ItemDto itemDto = new ItemDto(null, "", "Test Description", true, null);

        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_Success() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Original", "Original Desc", true, null);
        String response = mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andReturn().getResponse().getContentAsString();
        
        ItemDto created = objectMapper.readValue(response, ItemDto.class);

        ItemDto updateDto = new ItemDto(null, "Updated", null, null, null);
        mockMvc.perform(patch("/items/" + created.getId())
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.description", is("Original Desc")));
    }

    @Test
    void getItem_Success() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        String response = mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andReturn().getResponse().getContentAsString();
        
        ItemDto created = objectMapper.readValue(response, ItemDto.class);

        mockMvc.perform(get("/items/" + created.getId())
                .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.comments", notNullValue()));
    }

    @Test
    void getOwnerItems_Success() throws Exception {
        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ItemDto(null, "Item 1", "Desc 1", true, null))));

        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ItemDto(null, "Item 2", "Desc 2", true, null))));

        mockMvc.perform(get("/items")
                .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchItems_Success() throws Exception {
        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ItemDto(null, "Дрель", "Простая дрель", true, null))));

        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ItemDto(null, "Отвертка", "Отвертка аккумуляторная", true, null))));

        mockMvc.perform(get("/items/search")
                .header(USER_HEADER, ownerId)
                .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Дрель")));
    }

    @Test
    void searchItems_BlankText_ReturnsEmpty() throws Exception {
        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ItemDto(null, "Дрель", "Простая дрель", true, null))));

        mockMvc.perform(get("/items/search")
                .header(USER_HEADER, ownerId)
                .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
