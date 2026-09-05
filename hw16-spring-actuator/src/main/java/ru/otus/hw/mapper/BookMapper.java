package ru.otus.hw.mapper;

import ru.otus.hw.dto.BookDto;
import ru.otus.hw.model.Book;

import java.util.List;

public interface BookMapper {
    BookDto toBookDto(Book book);

    List<BookDto> toBookDtoList(List<Book> books);
}
