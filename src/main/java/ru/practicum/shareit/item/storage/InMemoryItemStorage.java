package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long id = 1;

    @Override
    public Item create(Item item) {
        item.setId(id++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        Long itemId = item.getId();
        if (!items.containsKey(itemId)) {
            throw new ru.practicum.shareit.exception.NotFoundException("Item with id=" + itemId + " was not found");
        }
        items.put(itemId, item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Collection<Item> getByOwner(Long ownerId) {
        return items.values().stream()
            .filter(item -> item.getOwner().getId().equals(ownerId))
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> search(String text) {
        String normalizedText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsText(item.getName(), normalizedText)
                        || containsText(item.getDescription(), normalizedText))
                .collect(Collectors.toList());
    }

    private boolean containsText(String value, String text) {
        return value != null && value.toLowerCase().contains(text);
    }
}
