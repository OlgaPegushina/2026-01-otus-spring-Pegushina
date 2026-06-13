package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.model.Book;
import ru.otus.hw.repository.BookRepository;

import java.util.HashSet;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    @Override
    public Mono<BookDto> findById(long id) {
        return bookRepository.findByIdWithDetails(id)
                .map(bookMapper::toBookDto)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(id))));
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllWithDetails().map(bookMapper::toBookDto);
    }

    @Override
    @Transactional
    public Mono<BookDto> create(BookCreateDto bookDto) {
        var genreIdsSet = new HashSet<>(bookDto.genreIds());

        var bookToSave = new Book(0L, bookDto.title(), bookDto.authorId(), null, null);

        return bookRepository.save(bookToSave)
                .flatMap(savedBook -> {
                    long bookId = savedBook.getId();
                    return Flux.fromIterable(genreIdsSet)
                            .flatMap(genreId -> bookRepository.saveGenreLink(bookId, genreId))
                            .then(findById(bookId));
                });
    }

    @Override
    @Transactional
    public Mono<BookDto> update(BookUpdateDto bookDto) {
        var genreIdsSet = new HashSet<>(bookDto.genreIds());

        return findBookOrError(bookDto.id())
                .flatMap(bookToUpdate -> {
                    bookToUpdate.setTitle(bookDto.title());
                    bookToUpdate.setAuthorId(bookDto.authorId());

                    return bookRepository.save(bookToUpdate)
                            .flatMap(savedBook -> {
                                long bookId = savedBook.getId();
                                return bookRepository.deleteGenreLinksByBookId(bookId)
                                        .thenMany(Flux.fromIterable(genreIdsSet))
                                        .flatMap(genreId -> bookRepository.saveGenreLink(bookId, genreId))
                                        .then(findById(bookId));
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(long id) {
        return bookRepository.deleteGenreLinksByBookId(id)
                .then(bookRepository.deleteById(id));
    }

    private Mono<Book> findBookOrError(long bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(bookId))));
    }
}