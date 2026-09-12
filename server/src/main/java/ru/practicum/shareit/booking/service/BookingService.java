package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.Collection;

public interface BookingService {
    BookingDto create(Long userId, BookingCreateDto bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    Collection<BookingDto> getUserBookings(Long userId, String state);

    Collection<BookingDto> getOwnerBookings(Long ownerId, String state);
}
