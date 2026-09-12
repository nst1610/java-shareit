package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "requestor", source = "requestor")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    ItemRequest toEntity(ItemRequestCreateDto requestDto, User requestor);

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "created", source = "request.created")
    @Mapping(target = "items", source = "items")
    ItemRequestDto toDto(ItemRequest request, List<Item> items);

    @Mapping(target = "ownerId", source = "owner.id")
    ItemResponseDto toItemResponseDto(Item item);
}
