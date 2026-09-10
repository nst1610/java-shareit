package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody BookingCreateDto bookingDto) {
        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long bookingId,
                                          @RequestParam boolean approved) {
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long bookingId) {
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        return bookingClient.getUserBookings(userId, BookingState.from(state));
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        return bookingClient.getOwnerBookings(userId, BookingState.from(state));
    }
}
