package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    @Override
    public BookDto findById(long id) {
        var book = findBookById(id);
        return bookMapper.toBookDto(book);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> findAll() {
        var books = bookRepository.findAll();
        return bookMapper.toBookDtoList(books);
    }

    @Transactional
    @Override
    public BookDto create(BookCreateDto bookDto) {
        var genreIdsSet = new HashSet<>(bookDto.genreIds());
        var bookParts = findBookParts(bookDto.authorId(), genreIdsSet);
        var book = new Book(0, bookDto.title(), bookParts.author(), bookParts.genres());
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Transactional
    @Override
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
}