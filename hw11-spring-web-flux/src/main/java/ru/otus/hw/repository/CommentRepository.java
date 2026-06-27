package ru.otus.hw.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.model.Comment;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    Flux<Comment> findAllByBookId(Long bookId);
}
