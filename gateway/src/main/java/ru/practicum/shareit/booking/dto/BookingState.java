package ru.practicum.shareit.booking.dto;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String value) {
        try {
            return value == null ? ALL : BookingState.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown state: " + value);
        }
    }
}
