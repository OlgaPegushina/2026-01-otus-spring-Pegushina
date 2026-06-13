package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.model.Author;

@Component
public class AuthorMapperImpl implements AuthorMapper {
    @Override
    public AuthorDto toAuthorDto(Author author) {
        if (author == null) {
            return null;
        }

        return new AuthorDto(author.getId(), author.getFullName());
    }
}
