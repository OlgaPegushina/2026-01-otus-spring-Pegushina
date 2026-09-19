package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.model.Author;

import java.util.Collections;
import java.util.List;

@Component
public class AuthorMapperImpl implements AuthorMapper {
    @Override
    public AuthorDto toAuthorDto(Author author) {
        if (author == null) {
            return null;
        }

        return new AuthorDto(author.getId(), author.getFullName());
    }

    @Override
    public List<AuthorDto> toAuthorDtoList(List<Author> authors) {
        if (authors == null) {
            return Collections.emptyList();
        }

        return authors.stream()
                .map(this::toAuthorDto)
                .toList();
    }
}
