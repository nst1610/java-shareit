package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = getUserOrThrow(ownerId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            item.setRequest(requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found: " + itemDto.getRequestId())));
        }
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(ownerId);
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("Only the owner can edit the item");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDetailsDto getById(Long userId, Long itemId) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        return toItemDetailsDto(item, userId);
    }

    @Override
    public Collection<ItemDetailsDto> getOwnerItems(Long ownerId) {
        getUserOrThrow(ownerId);
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<Comment>> commentsByItemId = commentRepository
                .findAllByItemIdInOrderByCreatedAsc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        Map<Long, List<Booking>> bookingsByItemId = bookingRepository
                .findAllByItemIdInAndStatusOrderByStartAsc(itemIds, BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        return items.stream()
                .map(item -> toItemDetailsDto(
                    item, ownerId,
                    commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()),
                    bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .toList();
    }

    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        getUserOrThrow(userId);
        if (text == null || text.isBlank()) {
            return java.util.List.of();
        }
        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        boolean hasCompletedBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasCompletedBooking) {
            throw new BadRequestException("User has not completed booking for item: " + itemId);
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        return commentMapper.toDto(commentRepository.save(comment));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }

    private ItemDetailsDto toItemDetailsDto(Item item, Long userId) {
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedAsc(item.getId());
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStartDesc(item.getId());
        return toItemDetailsDto(item, userId, comments, bookings);
    }

    private ItemDetailsDto toItemDetailsDto(Item item, Long userId, List<Comment> comments, List<Booking> bookings) {
        ItemDetailsDto itemDto = itemMapper.toItemDetailsDto(item);
        itemDto.setComments(comments.stream().map(commentMapper::toDto).toList());
        if (item.getOwner().getId().equals(userId)) {
            fillBookingDates(itemDto, bookings);
        }
        return itemDto;
    }

    private void fillBookingDates(ItemDetailsDto itemDto, List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.APPROVED)
                .filter(booking -> booking.getStart().isBefore(now))
                .reduce((first, second) -> second)
                .map(Booking::getStart)
                .ifPresent(itemDto::setLastBooking);
        bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.APPROVED)
                .filter(booking -> booking.getStart().isAfter(now))
                .findFirst()
                .map(Booking::getStart)
                .ifPresent(itemDto::setNextBooking);
    }
}
