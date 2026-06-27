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
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.GenreRepository;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

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
        if (genreIdsSet.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Genres ids must not be empty"));
        }

        return validateDependencies(bookDto, genreIdsSet)
                .then(Mono.defer(() -> saveBookAndLinks(bookDto, genreIdsSet)));
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

    private Mono<Void> validateDependencies(BookCreateDto bookDto, Set<Long> genreIdsSet) {
        Mono<Boolean> authorExists = authorRepository.existsById(bookDto.authorId());
        Mono<Long> foundGenresCount = genreRepository.countByIdIn(genreIdsSet);

        return Mono.zip(authorExists, foundGenresCount)
                .flatMap(tuple -> {
                    boolean isAuthorExists = tuple.getT1();
                    long dbGenresCount = tuple.getT2();

                    if (!isAuthorExists) {
                        return Mono.error(new EntityNotFoundException(
                                "Author with id %d not found".formatted(bookDto.authorId())));
                    }
                    if (dbGenresCount != genreIdsSet.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or more genres with ids %s not found".formatted(genreIdsSet)));
                    }
                    return Mono.empty();
                }).then(); //-- .then() преобразует Mono<Object> в Mono<Void>
    }

    private Mono<BookDto> saveBookAndLinks(BookCreateDto bookDto, Set<Long> genreIdsSet) {
        var bookToSave = new Book(0L, bookDto.title(), bookDto.authorId(), null, null);

        return bookRepository.save(bookToSave)
                .flatMap(savedBook ->
                        Flux.fromIterable(genreIdsSet)
                                .flatMap(genreId -> bookRepository.saveGenreLink(savedBook.getId(), genreId))
                                .then(findById(savedBook.getId()))
                );
    }
}