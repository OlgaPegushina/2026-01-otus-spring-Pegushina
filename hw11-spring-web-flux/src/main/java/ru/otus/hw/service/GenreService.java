package ru.otus.hw.service;

import reactor.core.publisher.Flux;
import ru.otus.hw.dto.GenreDto;

public interface GenreService {
    Flux<GenreDto> findAll();
}
