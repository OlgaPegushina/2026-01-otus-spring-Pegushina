package ru.otus.hw.mapper;

import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.model.Genre;

import java.util.List;

public interface GenreMapper {
    GenreDto toGenreDto(Genre genre);

    List<GenreDto> toGenreDtoList(List<Genre> genres);
}
