package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.model.Book;

import java.util.Collections;
import java.util.List;

@Component
public class BookMapperImpl implements BookMapper {
    @Override
    public BookDto toBookDto(Book book) {
        var author = book.getAuthor();
        var genres = book.getGenres();

        var authorDto = new AuthorDto(author.getId(), author.getFullName());
        var genreDtos = genres.stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();

        return new BookDto(book.getId(), book.getTitle(), authorDto, genreDtos);
    }

    @Override
    public List<BookDto> toBookDtoList(List<Book> books) {
        if (books == null) {
            return Collections.emptyList();
        }

        return books.stream()
                .map(this::toBookDto)
                .toList();
    }
}
