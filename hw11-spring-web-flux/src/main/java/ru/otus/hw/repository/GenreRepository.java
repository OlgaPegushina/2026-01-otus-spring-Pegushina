package ru.otus.hw.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.otus.hw.model.Genre;

import java.util.Set;

public interface GenreRepository extends ReactiveCrudRepository<Genre, Long> {
    Mono<Long> countByIdIn(Set<Long> genreIdsSet);
}
