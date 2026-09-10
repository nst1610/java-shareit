package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
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

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.create(eq(2L), any())).thenReturn(booking());
        BookingCreateDto request = BookingCreateDto.builder()
            .itemId(10L)
            .start(LocalDateTime.of(2026, 2, 1, 10, 0))
            .end(LocalDateTime.of(2026, 2, 2, 10, 0))
            .build();
        mockMvc.perform(post("/bookings")
                .header(USER_HEADER, 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(30));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        BookingDto approved = booking();
        approved.setStatus(BookingStatus.APPROVED);
        when(bookingService.approve(1L, 30L, true)).thenReturn(approved);

        mockMvc.perform(patch("/bookings/{id}", 30)
                .header(USER_HEADER, 1)
                .param("approved", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.getById(2L, 30L)).thenReturn(booking());

        mockMvc.perform(get("/bookings/{id}", 30).header(USER_HEADER, 2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(30));
    }

    @Test
    void shouldGetBookerBookings() throws Exception {
        when(bookingService.getUserBookings(2L, "FUTURE")).thenReturn(List.of(booking()));

        mockMvc.perform(get("/bookings")
                .header(USER_HEADER, 2)
                .param("state", "FUTURE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(1L, "ALL")).thenReturn(List.of(booking()));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    private BookingDto booking() {
        return BookingDto.builder()
            .id(30L)
            .start(LocalDateTime.of(2026, 2, 1, 10, 0))
            .end(LocalDateTime.of(2026, 2, 2, 10, 0))
            .status(BookingStatus.WAITING)
            .build();
    }
}
