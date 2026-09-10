package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import ru.practicum.shareit.request.ItemRequest;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId);
}
