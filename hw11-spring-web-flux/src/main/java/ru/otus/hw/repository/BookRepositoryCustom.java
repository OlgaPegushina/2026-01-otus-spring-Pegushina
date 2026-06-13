package ru.otus.hw.repository;

import reactor.core.publisher.Flux;
import ru.otus.hw.model.Book;

public interface BookRepositoryCustom {
    Flux<Book> findAllWithDetails();
}
