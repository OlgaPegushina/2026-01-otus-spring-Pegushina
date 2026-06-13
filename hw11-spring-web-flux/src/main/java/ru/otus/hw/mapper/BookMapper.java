package ru.otus.hw.mapper;

import ru.otus.hw.dto.BookDto;
import ru.otus.hw.model.Book;

public interface BookMapper {
    BookDto toBookDto(Book book);
}
