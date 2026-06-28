package ru.otus.hw.mapper;

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.model.Author;

import java.util.List;

public interface AuthorMapper {
    AuthorDto toAuthorDto(Author author);

    List<AuthorDto> toAuthorDtoList(List<Author> authors);
}
