package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.model.Book;

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
}
