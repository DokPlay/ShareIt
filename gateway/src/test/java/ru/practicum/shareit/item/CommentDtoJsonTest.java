package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testCommentDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 1, 9, 15, 45, 0);
        CommentDto dto = new CommentDto(1L, "Great item!", "John", created);

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-09T15:45:00");
    }

    @Test
    void testCommentDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"John\",\"created\":\"2026-01-09T15:45:00\"}";

        CommentDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Great item!");
        assertThat(dto.getAuthorName()).isEqualTo("John");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 1, 9, 15, 45, 0));
    }
}
