package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;
    private Item item;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = saveUser("Owner", "owner@example.com");
        booker = saveUser("Booker", "booker@example.com");
        request = requestRepository.save(ItemRequest.builder()
            .description("Need a drill")
            .requestor(booker)
            .created(LocalDateTime.now().minusDays(1))
            .build());
        item = itemRepository.save(Item.builder()
            .name("Drill")
            .description("Cordless power drill")
            .available(true)
            .owner(owner)
            .build());
    }

    @Test
    void createShouldPersistItemAndRequestReference() {
        ItemDto created = itemService.create(owner.getId(), ItemDto.builder()
            .name("Saw")
            .description("Circular saw")
            .available(true)
            .requestId(request.getId())
            .build());
        Item persisted = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(persisted.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(persisted.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void updateShouldChangeOnlyProvidedFields() {
        ItemDto updated = itemService.update(owner.getId(), item.getId(), ItemDto.builder()
            .description("Updated description")
            .build());
        assertThat(updated.getName()).isEqualTo("Drill");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void getByIdShouldReturnDetailsAndComments() {
        saveCompletedBooking();
        itemService.addComment(booker.getId(), item.getId(), CommentDto.builder()
            .text("Very useful")
            .build());
        ItemDetailsDto found = itemService.getById(owner.getId(), item.getId());
        assertThat(found.getComments())
            .extracting(CommentDto::getText)
            .containsExactly("Very useful");
        assertThat(found.getLastBooking()).isNotNull();
    }

    @Test
    void getOwnerItemsShouldReturnItemsWithBookingData() {
        saveCompletedBooking();
        assertThat(itemService.getOwnerItems(owner.getId()))
            .singleElement()
            .satisfies(result -> {
                assertThat(result.getId()).isEqualTo(item.getId());
                assertThat(result.getLastBooking()).isNotNull();
            });
    }

    @Test
    void searchShouldUseDatabaseQueryAndIgnoreUnavailableItems() {
        itemRepository.save(Item.builder()
            .name("Unavailable drill")
            .description("Another drill")
            .available(false)
            .owner(owner)
            .build());
        assertThat(itemService.search(booker.getId(), "DRILL"))
            .extracting(ItemDto::getId)
            .containsExactly(item.getId());
    }

    @Test
    void addCommentShouldPersistCommentAfterCompletedBooking() {
        saveCompletedBooking();
        CommentDto created = itemService.addComment(booker.getId(), item.getId(),
            CommentDto.builder().text("Recommended").build());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getAuthorName()).isEqualTo("Booker");
        assertThat(commentRepository.findById(created.getId())).isPresent();
    }

    private void saveCompletedBooking() {
        bookingRepository.save(Booking.builder()
            .start(LocalDateTime.now().minusDays(2))
            .end(LocalDateTime.now().minusDays(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.APPROVED)
            .build());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }
}
