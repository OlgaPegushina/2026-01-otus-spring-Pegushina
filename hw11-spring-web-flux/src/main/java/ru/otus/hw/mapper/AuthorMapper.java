package ru.otus.hw.mapper;

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.model.Author;

public interface AuthorMapper {
    AuthorDto toAuthorDto(Author author);
}
