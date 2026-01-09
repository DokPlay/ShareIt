package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void testBookingCreateDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 1, 10, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 11, 12, 0, 0);
        BookingCreateDto dto = new BookingCreateDto(1L, start, end);

        JsonContent<BookingCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-01-10T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-01-11T12:00:00");
    }

    @Test
    void testBookingCreateDtoDeserialization() throws Exception {
        String jsonContent = "{\"itemId\":1,\"start\":\"2026-01-10T12:00:00\",\"end\":\"2026-01-11T12:00:00\"}";

        BookingCreateDto dto = json.parseObject(jsonContent);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 1, 10, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 1, 11, 12, 0, 0));
    }
}
