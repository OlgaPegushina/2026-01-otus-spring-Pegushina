package ru.otus.hw.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.otus.hw.model.Genre;

public interface GenreRepository extends ReactiveCrudRepository<Genre, Long> {
}
