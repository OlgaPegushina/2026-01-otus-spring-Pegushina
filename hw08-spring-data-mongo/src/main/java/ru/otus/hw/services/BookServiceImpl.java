package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private record BookParts(Author author, List<Genre> genres) {}

    @Override
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book insert(String title, String authorId, Set<String> genresIds) {
        var bookParts = findBookParts(authorId, genresIds);

        var book = new Book(null, title, bookParts.author(), bookParts.genres());
        return bookRepository.save(book);
    }

    @Override
    public Book update(String id, String title, String authorId, Set<String> genresIds) {
        Book bookToUpdate = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));

        var bookParts = findBookParts(authorId, genresIds);

        bookToUpdate.setTitle(title);
        bookToUpdate.setAuthor(bookParts.author());
        bookToUpdate.setGenres(bookParts.genres());

        return bookRepository.save(bookToUpdate);
    }

    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

    private BookParts findBookParts(String authorId, Set<String> genresIds) {
        var author = findAuthorById(authorId);
        var genres = findGenresByIds(genresIds);
        return new BookParts(author, genres);
    }

    private Author findAuthorById(String authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(authorId)));
    }

    private List<Genre> findGenresByIds(Set<String> genresIds) {
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
