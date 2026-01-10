package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto requestor;
    private UserDto owner;

    @BeforeEach
    void setUp() {
        requestor = userService.create(new UserDto(null, "Requestor", "req@mail.com"));
        owner = userService.create(new UserDto(null, "Owner", "own@mail.com"));
    }

    @Test
    void createRequest_Success() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        
        ItemRequestDto created = requestService.createRequest(requestor.getId(), dto);
        
        assertNotNull(created.getId());
        assertEquals(dto.getDescription(), created.getDescription());
        assertNotNull(created.getCreated());
    }

    @Test
    void getUserRequests_Success() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        requestService.createRequest(requestor.getId(), dto);

        List<ItemRequestDto> requests = requestService.getUserRequests(requestor.getId());
        
        assertEquals(1, requests.size());
        assertEquals("Need a drill", requests.get(0).getDescription());
    }
    
    @Test
    void getAllRequests_Success() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        requestService.createRequest(requestor.getId(), dto);
        
        // Other user sees it
        List<ItemRequestDto> all = requestService.getAllRequests(owner.getId(), 0, 10);
        assertEquals(1, all.size());
        
        // Requestor doesn't see their own in "all" (meaning "others requests")
        List<ItemRequestDto> self = requestService.getAllRequests(requestor.getId(), 0, 10);
        assertEquals(0, self.size());
    }
    
    @Test
    void getRequestById_Success() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDto created = requestService.createRequest(requestor.getId(), dto);
        
        // Add item response
        ItemDto itemDto = new ItemDto(null, "Drill", "Good drill", true, created.getId());
        itemService.create(owner.getId(), itemDto);
        
        ItemRequestDto found = requestService.getRequestById(requestor.getId(), created.getId());
        
        assertEquals(created.getId(), found.getId());
        assertEquals(1, found.getItems().size());
        assertEquals("Drill", found.getItems().get(0).getName());
    }
}