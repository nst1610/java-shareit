package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = saveUser("Owner", "booking-owner@example.com");
        booker = saveUser("Booker", "booking-booker@example.com");
        item = itemRepository.save(Item.builder()
            .name("Tent")
            .description("Two-person tent")
            .available(true)
            .owner(owner)
            .build());
        booking = bookingRepository.save(Booking.builder()
            .start(LocalDateTime.now().plusDays(2))
            .end(LocalDateTime.now().plusDays(3))
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build());
    }

    @Test
    void createShouldPersistWaitingBooking() {
        BookingDto created = bookingService.create(booker.getId(), BookingCreateDto.builder()
            .itemId(item.getId())
            .start(LocalDateTime.now().plusDays(4))
            .end(LocalDateTime.now().plusDays(5))
            .build());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookingRepository.findById(created.getId())).isPresent();
    }

    @Test
    void approveShouldUpdateBookingStatus() {
        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
            .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getByIdShouldReturnBookingForBooker() {
        BookingDto found = bookingService.getById(booker.getId(), booking.getId());
        assertThat(found.getId()).isEqualTo(booking.getId());
        assertThat(found.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getUserBookingsShouldFilterByState() {
        assertThat(bookingService.getUserBookings(booker.getId(), "FUTURE"))
            .extracting(BookingDto::getId)
            .containsExactly(booking.getId());
    }

    @Test
    void getOwnerBookingsShouldReturnBookingsForOwnedItems() {
        assertThat(bookingService.getOwnerBookings(owner.getId(), "ALL"))
            .extracting(BookingDto::getId)
            .containsExactly(booking.getId());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }
}
