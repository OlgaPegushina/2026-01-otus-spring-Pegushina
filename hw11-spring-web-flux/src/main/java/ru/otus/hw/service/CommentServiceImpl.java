package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.model.Book;
import ru.otus.hw.model.Comment;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final CommentMapper commentMapper;

    @Override
    public Mono<CommentDto> findById(long id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Comment with id %d not found".formatted(id))))
                .map(commentMapper::toCommentDto);
    }

    @Override
    public Flux<CommentDto> findAllByBookId(long bookId) {
        return checkBookExists(bookId)
                .thenMany(commentRepository.findAllByBookId(bookId)
                .map(commentMapper::toCommentDto));
    }

    @Override
    public Mono<CommentDto> create(CommentCreateDto commentDto) {
        return findBookOrError(commentDto.bookId())
                .flatMap(book -> {
                    var commentToSave = new Comment(0L, commentDto.text(), book.getId(), book);
                    return commentRepository.save(commentToSave);
                })
                .map(commentMapper::toCommentDto);
    }

    @Override
    public Mono<CommentDto> update(CommentUpdateDto commentDto) {
        return commentRepository.findById(commentDto.id())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %d not found".formatted(commentDto.id()))))
                .flatMap(commentToUpdate -> {
                    commentToUpdate.setText(commentDto.text());
                    return commentRepository.save(commentToUpdate);
                })
                .map(commentMapper::toCommentDto);
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return commentRepository.deleteById(id);
    }

    private Mono<Void> checkBookExists(long bookId) {
        return bookRepository.existsById(bookId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(bookId))))
                .then();
    }

    private Mono<Book> findBookOrError(long bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId))));
    }
}
