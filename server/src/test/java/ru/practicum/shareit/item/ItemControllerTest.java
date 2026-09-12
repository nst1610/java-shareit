package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto item = item();
        when(itemService.create(eq(1L), any())).thenReturn(item);
        mockMvc.perform(post("/items")
                .header(USER_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.update(eq(1L), eq(10L), any())).thenReturn(item());
        mockMvc.perform(patch("/items/{id}", 10)
                .header(USER_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Drill\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemService.getById(1L, 10L)).thenReturn(details());
        mockMvc.perform(get("/items/{id}", 10).header(USER_HEADER, 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comments.length()").value(0));
    }

    @Test
    void shouldGetOwnerItems() throws Exception {
        when(itemService.getOwnerItems(1L)).thenReturn(List.of(details()));
        mockMvc.perform(get("/items").header(USER_HEADER, 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.search(1L, "drill")).thenReturn(List.of(item()));
        mockMvc.perform(get("/items/search")
                .header(USER_HEADER, 1)
                .param("text", "drill"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto comment = CommentDto.builder()
            .id(20L)
            .text("Works well")
            .authorName("Booker")
            .created(LocalDateTime.of(2026, 1, 2, 3, 4))
            .build();
        when(itemService.addComment(eq(2L), eq(10L), any())).thenReturn(comment);
        mockMvc.perform(post("/items/{id}/comment", 10)
                .header(USER_HEADER, 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Works well\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorName").value("Booker"));
    }

    private ItemDto item() {
        return ItemDto.builder()
            .id(10L)
            .name("Drill")
            .description("Cordless drill")
            .available(true)
            .build();
    }

    private ItemDetailsDto details() {
        return ItemDetailsDto.builder()
            .id(10L)
            .name("Drill")
            .description("Cordless drill")
            .available(true)
            .comments(List.of())
            .build();
    }
}
