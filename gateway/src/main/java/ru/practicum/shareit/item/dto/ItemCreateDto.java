package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {
    @NotBlank(message = "Item name must not be blank")
    private String name;

    @NotBlank(message = "Item description must not be blank")
    private String description;

    @NotNull(message = "Item availability must not be null")
    private Boolean available;

    private Long requestId;
}
