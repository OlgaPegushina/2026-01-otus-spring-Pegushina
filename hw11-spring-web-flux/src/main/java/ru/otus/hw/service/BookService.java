package ru.otus.hw.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;

public interface BookService {
    Mono<BookDto> findById(long id);

    Flux<BookDto> findAll();

    Mono<BookDto> create(BookCreateDto bookDto);

    Mono<BookDto> update(BookUpdateDto bookDto);

    Mono<Void> deleteById(long id);
}
