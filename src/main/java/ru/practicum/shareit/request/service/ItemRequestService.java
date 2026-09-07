package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestCreateDto requestDto);

    Collection<ItemRequestDto> getOwnRequests(Long userId);

    Collection<ItemRequestDto> getOtherUsersRequests(Long userId);

    ItemRequestDto getById(Long userId, Long requestId);
}
