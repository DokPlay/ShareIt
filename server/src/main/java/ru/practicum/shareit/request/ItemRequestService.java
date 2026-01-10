package ru.practicum.shareit.request;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto dto);
    List<ItemRequestDto> getUserRequests(Long userId);
    List<ItemRequestDto> getAllRequests(Long userId, int from, int size);
    ItemRequestDto getRequestById(Long userId, Long requestId);
}
