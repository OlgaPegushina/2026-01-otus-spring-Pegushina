package ru.otus.hw.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;

public interface CommentService {
    Mono<CommentDto> findById(long id);

    Flux<CommentDto> findAllByBookId(long bookId);

    Mono<CommentDto> create(CommentCreateDto commentDto);

    Mono<CommentDto> update(CommentUpdateDto commentDto);

    Mono<Void> deleteById(long id);
}
