package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = getUserOrThrow(ownerId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        return itemMapper.toItemDto(itemStorage.create(item));
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(ownerId);
        Item item= getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("Only the owner can edit the item");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return itemMapper.toItemDto(itemStorage.update(item));
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        return itemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> getOwnerItems(Long ownerId) {
        getUserOrThrow(ownerId);
        return itemStorage.getByOwner(ownerId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        getUserOrThrow(userId);
        if (text == null || text.isBlank()) {
            return java.util.List.of();
        }
        return itemStorage.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }
}
