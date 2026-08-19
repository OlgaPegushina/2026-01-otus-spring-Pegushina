package ru.otus.hw.mapper;

import ru.otus.hw.domain.jpa.AuthorEntity;
import ru.otus.hw.domain.jpa.BookEntity;
import ru.otus.hw.domain.mongo.BookDoc;

import java.util.List;

public interface BookMapper {
    BookDoc toDoc(BookEntity bookEntity, AuthorEntity author, List<Long> genreIds);
}
