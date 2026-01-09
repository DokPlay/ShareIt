package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemDto;
import java.util.List;
import java.util.ArrayList;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest request, List<ItemDto> items) {
        return new ItemRequestDto(
            request.getId(),
            request.getDescription(),
            request.getCreated(),
            items != null ? items : new ArrayList<>()
        );
    }
    
    public static ItemRequestDto toDto(ItemRequest request) {
        return toDto(request, null);
    }
}
