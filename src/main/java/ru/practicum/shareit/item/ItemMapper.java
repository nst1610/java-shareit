package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "request", source = "request.id")
    ItemDto toItemDto(Item item);

    @Mapping(target = "request", source = "request.id")
    ItemDetailsDto toItemDetailsDto(Item item);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    Item toItem(ItemDto itemDto);
}
