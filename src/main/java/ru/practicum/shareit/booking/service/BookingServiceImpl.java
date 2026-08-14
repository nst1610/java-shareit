package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateDto bookingDto) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());
        validateBooking(bookingDto, item, userId);
        Booking booking = bookingMapper.toEntity(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ConflictException("Only item owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Booking status has already been updated");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        getUserOrThrow(userId);
        Booking booking = getBookingOrThrow(bookingId);
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Booking is not accessible for user: " + userId);
        }
        return bookingMapper.toDto(booking);
    }

    @Override
    public Collection<BookingDto> getUserBookings(Long userId, String state) {
        getUserOrThrow(userId);
        BookingState bookingState = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();
        return getUserBookingsByState(userId, bookingState, now).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public Collection<BookingDto> getOwnerBookings(Long ownerId, String state) {
        getUserOrThrow(ownerId);
        BookingState bookingState = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();
        return getOwnerBookingsByState(ownerId, bookingState, now).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    private Collection<Booking> getUserBookingsByState(Long userId, BookingState state, LocalDateTime now) {
        return switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
    }

    private Collection<Booking> getOwnerBookingsByState(Long ownerId, BookingState state, LocalDateTime now) {
        return switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId);
            case CURRENT -> bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    ownerId, now, now);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now);
            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        };
    }

    private void validateBooking(BookingCreateDto bookingDto, Item item, Long userId) {
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new BadRequestException("Booking end must be after booking start");
        }
        if (!item.getAvailable()) {
            throw new ConflictException("Item is not available for booking");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ConflictException("Owner cannot book own item");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
    }
}
