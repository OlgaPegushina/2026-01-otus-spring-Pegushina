package ru.otus.hw.service;

import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;

public interface AuthorService {
    Flux<AuthorDto> findAll();
}
