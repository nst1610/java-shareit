package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
    @NotNull(message = "Booking start must not be null")
    @FutureOrPresent(message = "Booking start must be in the future")
    private LocalDateTime start;

    @NotNull(message = "Booking end must not be null")
    @Future(message = "Booking end must be in the future")
    private LocalDateTime end;

    @NotNull(message = "Item id must not be null")
    private Long itemId;
}
