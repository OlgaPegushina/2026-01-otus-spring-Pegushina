package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.model.Author;
import ru.otus.hw.model.Book;
import ru.otus.hw.model.Genre;
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private record BookParts(Author author, List<Genre> genres) {
    }

    @Override
    public Mono<BookDto> findById(long id) {
        return findBookOrError(id)
                .map(bookMapper::toBookDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllWithDetails().map(bookMapper::toBookDto);
    }

    @Override
    public Mono<BookDto> create(BookCreateDto bookDto) {
        var genreIdsSet = new HashSet<>(bookDto.genreIds());

        return findBookParts(bookDto.authorId(), genreIdsSet)
                .flatMap(bookParts -> {
                    var bookToSave = new Book(
                            0L,
                            bookDto.title(),
                            bookParts.author().getId(),
                            bookParts.author(),
                            bookParts.genres()
                    );
                    return bookRepository.save(bookToSave);
                })
                .map(bookMapper::toBookDto);
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto bookDto) {
        var genreIdsSet = new HashSet<>(bookDto.genreIds());

        Mono<Book> bookToUpdateMono = findBookOrError(bookDto.id());
        Mono<BookParts> newBookPartsMono = findBookParts(bookDto.authorId(), genreIdsSet);

        return Mono.zip(bookToUpdateMono, newBookPartsMono)
                .flatMap(tuple -> {
                    Book bookToUpdate = tuple.getT1();
                    BookParts newParts = tuple.getT2();

                    bookToUpdate.setTitle(bookDto.title());
                    bookToUpdate.setAuthor(newParts.author());
                    bookToUpdate.setGenres(newParts.genres());

                    return bookRepository.save(bookToUpdate);
                })
                .map(bookMapper::toBookDto);
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return bookRepository.deleteById(id);
    }

    private Mono<BookParts> findBookParts(long authorId, Set<Long> genreIds) {
        var author = findAuthorOrError(authorId);
        var genres = findGenresOrError(genreIds).collectList();
        return Mono.zip(author, genres)
                .map(tuple -> {
                    Author authorRes = tuple.getT1();
                    List<Genre> genresRes = tuple.getT2();
                    return new BookParts(authorRes, genresRes);
                });
    }

    private Mono<Book> findBookOrError(long bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(bookId))));
    }

    private Mono<Author> findAuthorOrError(long authorId) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %d not found".formatted(authorId))));
    }

    private Flux<Genre> findGenresOrError(Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
           return  Flux.error(new IllegalArgumentException("Genres ids must not be null or empty"));
        }

        return genreRepository.findAllById(genresIds)
                .collectList()
                .flatMapMany(foundGenres -> {
                    if (foundGenres.size() != genresIds.size()) {
                        return Flux.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genresIds)));
                    }
                    return Flux.fromIterable(foundGenres);
                });
    }
}