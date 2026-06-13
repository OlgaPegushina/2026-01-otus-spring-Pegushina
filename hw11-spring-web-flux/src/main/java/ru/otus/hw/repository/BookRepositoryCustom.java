package ru.otus.hw.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.model.Book;

public interface BookRepositoryCustom {
    Flux<Book> findAllWithDetails();

    Mono<Book> findByIdWithDetails(long bookId);

    Mono<Void> deleteGenreLinksByBookId(long bookId);

    Mono<Void> saveGenreLink(long bookId, long genreId);
}
