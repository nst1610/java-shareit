package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User responder;
    private ItemRequest ownRequest;
    private ItemRequest otherRequest;

    @BeforeEach
    void setUp() {
        requestor = saveUser("Requestor", "requestor@example.com");
        responder = saveUser("Responder", "responder@example.com");
        ownRequest = saveRequest("Need a drill", requestor, LocalDateTime.now().minusHours(2));
        otherRequest = saveRequest("Need a tent", responder, LocalDateTime.now().minusHours(1));
        itemRepository.save(Item.builder()
            .name("Drill")
            .description("Cordless drill")
            .available(true)
            .owner(responder)
            .request(ownRequest)
            .build());
    }

    @Test
    void createShouldPersistRequestForUser() {
        ItemRequestDto created = requestService.create(requestor.getId(),
            new ItemRequestCreateDto("Need a ladder"));
        ItemRequest persisted = requestRepository.findById(created.getId()).orElseThrow();
        assertThat(persisted.getRequestor().getId()).isEqualTo(requestor.getId());
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void getOwnRequestsShouldReturnResponsesAndSortNewestFirst() {
        ItemRequest newer = saveRequest("Need a saw", requestor, LocalDateTime.now());
        assertThat(requestService.getOwnRequests(requestor.getId()))
            .extracting(ItemRequestDto::getId)
            .containsExactly(newer.getId(), ownRequest.getId());
        assertThat(requestService.getOwnRequests(requestor.getId()))
            .filteredOn(dto -> dto.getId().equals(ownRequest.getId()))
            .singleElement()
            .satisfies(dto -> assertThat(dto.getItems()).singleElement()
                .satisfies(item -> assertThat(item.getOwnerId()).isEqualTo(responder.getId())));
    }

    @Test
    void getOtherUsersRequestsShouldExcludeOwnRequests() {
        assertThat(requestService.getOtherUsersRequests(requestor.getId()))
            .extracting(ItemRequestDto::getId)
            .containsExactly(otherRequest.getId());
    }

    @Test
    void getByIdShouldReturnRequestWithResponses() {
        ItemRequestDto found = requestService.getById(requestor.getId(), ownRequest.getId());
        assertThat(found.getDescription()).isEqualTo("Need a drill");
        assertThat(found.getItems())
            .singleElement()
            .satisfies(item -> assertThat(item.getName()).isEqualTo("Drill"));
    }

    private ItemRequest saveRequest(String description, User user, LocalDateTime created) {
        return requestRepository.save(ItemRequest.builder()
            .description(description)
            .requestor(user)
            .created(created)
            .build());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }
}
