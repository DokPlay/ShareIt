package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 1, 9, 10, 30, 0);
        ItemRequestDto dto = new ItemRequestDto(1L, "Need a drill", created, new ArrayList<>());

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-09T10:30:00");
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    void testItemRequestDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2026-01-09T10:30:00\",\"items\":[]}";

        ItemRequestDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 1, 9, 10, 30, 0));
        assertThat(dto.getItems()).isEmpty();
    }
}
