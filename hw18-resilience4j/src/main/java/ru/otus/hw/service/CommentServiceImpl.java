package ru.otus.hw.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.exception.ServiceUnavailableException;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.model.Book;
import ru.otus.hw.model.Comment;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.CommentRepository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    private static final String CB_NAME = "commentService";

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "findByIdFallback")
    @Retry(name = CB_NAME)
    public CommentDto findById(long id) {
        return commentMapper.toCommentDto(commentRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id))));

    }

    @Transactional(readOnly = true)
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "findAllByBookIdFallback")
    @Retry(name = CB_NAME)
    public List<CommentDto> findAllByBookId(long bookId) {
        findBookOrThrow(bookId);
        return commentMapper.toCommentDtoList(commentRepository.findAllByBookId(bookId));
    }

    @Transactional
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "createFallback")
    @Retry(name = CB_NAME)
    public CommentDto create(CommentCreateDto commentDto) {
        var book = findBookOrThrow(commentDto.bookId());

        var comment = new Comment(0, commentDto.text(), book);

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "updateFallback")
    @Retry(name = CB_NAME)
    public CommentDto update(CommentUpdateDto commentDto) {
        Comment commentToUpdate = commentRepository.findById(commentDto.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id %d not found".formatted(commentDto.id())));

        commentToUpdate.setText(commentDto.text());

        return commentMapper.toCommentDto(commentRepository.save(commentToUpdate));
    }

    @Transactional
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "deleteByIdFallback")
    @Retry(name = CB_NAME)
    public void deleteById(long id) {
        commentRepository.deleteById(id);
    }

    private Book findBookOrThrow(long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId)));
    }

    private CommentDto findByIdFallback(long id, Throwable ex) {
        log.warn("Деградация CommentService.findById(id={}): комментарий временно недоступен.", id);
        log.debug("Причина деградации CommentService.findById(id={})", id, ex);
        throw new ServiceUnavailableException("Комментарий временно недоступен. Попробуйте позже.");
    }

    private List<CommentDto> findAllByBookIdFallback(long bookId, Throwable ex) {
        log.warn("Деградация CommentService.findAllByBookId(bookId={}): список комментариев временно недоступен. " +
                 "Возвращаю пустой список.",
                bookId);
        log.debug("Причина деградации CommentService.findAllByBookId(bookId={})", bookId, ex);
        return Collections.emptyList();
    }

    private CommentDto createFallback(CommentCreateDto commentDto, Throwable ex) {
        log.warn("Деградация CommentService.create(): создание комментария временно недоступно.");
        log.debug("Причина деградации CommentService.create(), payload={}", commentDto, ex);
        throw new ServiceUnavailableException("Создание комментария временно недоступно. Попробуйте позже.");
    }

    private CommentDto updateFallback(CommentUpdateDto commentDto, Throwable ex) {
        log.warn("Деградация CommentService.update(id={}): обновление комментария временно недоступно.",
                commentDto.id());
        log.debug("Причина деградации CommentService.update(id={}), payload={}", commentDto.id(), commentDto, ex);
        throw new ServiceUnavailableException("Обновление комментария временно недоступно. Попробуйте позже.");
    }

    private void deleteByIdFallback(long id, Throwable ex) {
        log.warn("Деградация CommentService.deleteById(id={}): удаление комментария временно недоступно.", id);
        log.debug("Причина деградации CommentService.deleteById(id={})", id, ex);
        throw new ServiceUnavailableException("Удаление комментария временно недоступно. Попробуйте позже.");
    }
}
