package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentUpdateDto(
        @NotNull
        Long id,

        @NotBlank(message = "Текст комментария не может быть пустым")
        @Size(min = 3, max = 255, message = "Длина комментария должна быть от 3 до 255 символов")
        String text
) {
}
