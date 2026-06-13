package ru.otus.hw.mapper;

import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.model.Genre;

public interface GenreMapper {
    GenreDto toGenreDto(Genre genre);
}
