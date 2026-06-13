package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookCreateDto(
        @NotBlank(message = "Название книги не может быть пустым")
        @Size(min = 2, max = 255, message = "Название должно содержать от 2 до 255 символов")
        String title,

        @NotNull(message = "Нужно выбрать автора")
        Long authorId,

        @NotEmpty(message = "Нужно выбрать хотя бы один жанр")
        List<Long> genreIds
) {
}
