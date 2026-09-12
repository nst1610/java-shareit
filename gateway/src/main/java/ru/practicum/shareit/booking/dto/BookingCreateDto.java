package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
    @NotNull(message = "Booking start must not be null")
    @FutureOrPresent(message = "Booking start must be in the future or present")
    private LocalDateTime start;

    @NotNull(message = "Booking end must not be null")
    @Future(message = "Booking end must be in the future")
    private LocalDateTime end;

    @NotNull(message = "Item id must not be null")
    private Long itemId;

    @JsonIgnore
    @AssertTrue(message = "Booking end must be after booking start")
    public boolean isPeriodValid() {
        return start == null || end == null || end.isAfter(start);
    }
}
