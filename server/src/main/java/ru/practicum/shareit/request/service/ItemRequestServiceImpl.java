package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper requestMapper;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto requestDto) {
        User requestor = getUserOrThrow(userId);
        ItemRequest request = ItemRequest.builder()
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        return requestMapper.toDto(requestRepository.save(request), List.of());
    }

    @Override
    public Collection<ItemRequestDto> getOwnRequests(Long userId) {
        getUserOrThrow(userId);
        return toDtos(requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    public Collection<ItemRequestDto> getOtherUsersRequests(Long userId) {
        getUserOrThrow(userId);
        return toDtos(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId));
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        return requestMapper.toDto(request, itemRepository.findAllByRequestId(requestId));
    }

    private Collection<ItemRequestDto> toDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> requestMapper.toDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
