package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Item create(Item item);

    Item update(Item item);

    Optional<Item> findById(Long itemId);

    Collection<Item> getByOwner(Long ownerId);

    Collection<Item> search(String text);
}
