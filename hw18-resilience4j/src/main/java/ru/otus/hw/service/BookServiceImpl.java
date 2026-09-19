package ru.otus.hw.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.exception.ServiceUnavailableException;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.model.Author;
import ru.otus.hw.model.Book;
import ru.otus.hw.model.Genre;
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.GenreRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookServiceImpl implements BookService {
    private static final String CB_NAME = "bookService";

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private record BookParts(Author author, List<Genre> genres) {
    }

    @Transactional(readOnly = true)
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "findByIdFallback")
    @Retry(name = CB_NAME)
    public BookDto findById(long id) {
        var book = findBookById(id);
        return bookMapper.toBookDto(book);
    }

    @Transactional(readOnly = true)
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "findAllFallback")
    @Retry(name = CB_NAME)
    public List<BookDto> findAll() {
        var books = bookRepository.findAll();
        return bookMapper.toBookDtoList(books);
    }

    @Transactional
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "createFallback")
    @Retry(name = CB_NAME)
    public BookDto create(BookCreateDto bookDto) {
        var genreIdsSet = new HashSet<>(bookDto.genreIds());
        var bookParts = findBookParts(bookDto.authorId(), genreIdsSet);
        var book = new Book(0, bookDto.title(), bookParts.author(), bookParts.genres());
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Transactional
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "updateFallback")
    @Retry(name = CB_NAME)
    public BookDto update(BookUpdateDto bookDto) {
        Book bookToUpdate = findBookById(bookDto.id());

        var genreIdsSet = new HashSet<>(bookDto.genreIds());
        var bookParts = findBookParts(bookDto.authorId(), genreIdsSet);
        bookToUpdate.setTitle(bookDto.title());
        bookToUpdate.setAuthor(bookParts.author());
        bookToUpdate.setGenres(bookParts.genres());
        return bookMapper.toBookDto(bookRepository.save(bookToUpdate));
    }

    @Transactional
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "deleteByIdFallback")
    @Retry(name = CB_NAME)
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    private BookParts findBookParts(long authorId, Set<Long> genreIds) {
        var author = findAuthorById(authorId);
        var genres = findGenresByIds(genreIds);
        return new BookParts(author, genres);
    }

    private Book findBookById(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
    }

    private Author findAuthorById(long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
    }

    private List<Genre> findGenresByIds(Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null or empty");
        }
        var genres = genreRepository.findAllById(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }
        return genres;
    }

    private BookDto findByIdFallback(long id, Throwable ex) {
        log.warn("Деградация BookService.findById(id={}): книга временно недоступна.", id);
        log.debug("Причина деградации BookService.findById(id={})", id, ex);
        throw new ServiceUnavailableException("Книга временно недоступна. Попробуйте позже.");
    }

    private List<BookDto> findAllFallback(Throwable ex) {
        log.warn("Деградация BookService.findAll(): список книг временно недоступен. Возвращаю пустой список.");
        log.debug("Причина деградации BookService.findAll()", ex);
        return Collections.emptyList();
    }

    private BookDto createFallback(BookCreateDto bookDto, Throwable ex) {
        log.warn("Деградация BookService.create(): создание книги временно недоступно.");
        log.debug("Причина деградации BookService.create(), payload={}", bookDto, ex);
        throw new ServiceUnavailableException("Создание книги временно недоступно. Попробуйте позже.");
    }

    private BookDto updateFallback(BookUpdateDto bookDto, Throwable ex) {
        log.warn("Деградация BookService.update(id={}): обновление книги временно недоступно.", bookDto.id());
        log.debug("Причина деградации BookService.update(id={}), payload={}", bookDto.id(), bookDto, ex);
        throw new ServiceUnavailableException("Обновление книги временно недоступно. Попробуйте позже.");
    }

    private void deleteByIdFallback(long id, Throwable ex) {
        log.warn("Деградация BookService.deleteById(id={}): удаление книги временно недоступно.", id);
        log.debug("Причина деградации BookService.deleteById(id={})", id, ex);
        throw new ServiceUnavailableException("Удаление книги временно недоступно. Попробуйте позже.");
    }
}