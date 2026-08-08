package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.domain.jpa.AuthorEntity;
import ru.otus.hw.domain.jpa.BookEntity;
import ru.otus.hw.domain.mongo.BookDoc;

import java.util.List;

@Component
public class BookMapperImpl implements BookMapper {
    @Override
    public BookDoc toDoc(BookEntity bookEntity, AuthorEntity author, List<Long> genreIds) {
        return new BookDoc(
                String.valueOf(bookEntity.getId()),
                bookEntity.getTitle(),
                String.valueOf(author.getId()),
                genreIds.stream().map(String::valueOf).toList()
        );
    }
}
