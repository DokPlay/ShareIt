package ru.practicum.shareit.request;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private Long requestorId;
    private Long ownerId;

    @BeforeEach
    void setUp() throws Exception {
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Create requestor
        UserDto requestorDto = new UserDto(null, "Requestor", "requestor@email.com");
        String requestorResponse = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestorDto)))
                .andReturn().getResponse().getContentAsString();
        requestorId = objectMapper.readValue(requestorResponse, UserDto.class).getId();

        // Create owner
        UserDto ownerDto = new UserDto(null, "Owner", "owner@email.com");
        String ownerResponse = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerDto)))
                .andReturn().getResponse().getContentAsString();
        ownerId = objectMapper.readValue(ownerResponse, UserDto.class).getId();
    }

    @Test
    void createRequest_Success() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, requestorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is("Need a drill")))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void getUserRequests_Success() throws Exception {
        // Create request
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);
        mockMvc.perform(post("/requests")
                .header(USER_HEADER, requestorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Need a drill")));
    }

    @Test
    void getAllRequests_Success() throws Exception {
        // Create request by requestor
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);
        mockMvc.perform(post("/requests")
                .header(USER_HEADER, requestorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // Owner sees requestor's request
        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Requestor doesn't see their own request in 'all'
        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getRequestById_Success() throws Exception {
        // Create request
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);
        String response = mockMvc.perform(post("/requests")
                .header(USER_HEADER, requestorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn().getResponse().getContentAsString();

        ItemRequestDto created = objectMapper.readValue(response, ItemRequestDto.class);

        mockMvc.perform(get("/requests/" + created.getId())
                        .header(USER_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.description", is("Need a drill")));
    }

    @Test
    void getRequestById_WithItems_Success() throws Exception {
        // Create request
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);
        String response = mockMvc.perform(post("/requests")
                .header(USER_HEADER, requestorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn().getResponse().getContentAsString();

        ItemRequestDto created = objectMapper.readValue(response, ItemRequestDto.class);

        // Owner adds item in response
        ItemDto itemDto = new ItemDto(null, "Drill", "Good drill", true, created.getId());
        mockMvc.perform(post("/items")
                .header(USER_HEADER, ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)));

        mockMvc.perform(get("/requests/" + created.getId())
                        .header(USER_HEADER, requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("Drill")));
    }

    @Test
    void getRequestById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/requests/999")
                        .header(USER_HEADER, requestorId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequest_UserNotFound_Returns404() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
}
