package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    ItemDetailsDto getById(Long userId, Long itemId);

    Collection<ItemDetailsDto> getOwnerItems(Long ownerId);

    Collection<ItemDto> search(Long userId, String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
