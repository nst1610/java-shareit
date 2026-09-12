package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void shouldCreateRequest() throws Exception {
        when(requestService.create(eq(1L), any())).thenReturn(request());
        mockMvc.perform(post("/requests")
                .header(USER_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Need a drill\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(request()));
        mockMvc.perform(get("/requests").header(USER_HEADER, 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetOtherUsersRequests() throws Exception {
        when(requestService.getOtherUsersRequests(1L)).thenReturn(List.of(request()));
        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(40));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestService.getById(1L, 40L)).thenReturn(request());
        mockMvc.perform(get("/requests/{id}", 40).header(USER_HEADER, 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(0));
    }

    private ItemRequestDto request() {
        return ItemRequestDto.builder()
            .id(40L)
            .description("Need a drill")
            .created(LocalDateTime.of(2026, 1, 1, 12, 0))
            .items(List.of())
            .build();
    }
}
