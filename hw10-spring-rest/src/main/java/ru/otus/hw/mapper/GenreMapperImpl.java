package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.model.Genre;

import java.util.Collections;
import java.util.List;

@Component
public class GenreMapperImpl implements GenreMapper {
    @Override
    public GenreDto toGenreDto(Genre genre) {
        if (genre == null) {
            return null;
        }

        return new GenreDto(genre.getId(), genre.getName());
    }

    @Override
    public List<GenreDto> toGenreDtoList(List<Genre> genres) {
        if (genres == null) {
            return Collections.emptyList();
        }

        return genres.stream()
                .map(this::toGenreDto)
                .toList();
    }
}
