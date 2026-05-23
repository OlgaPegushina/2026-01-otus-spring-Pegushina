package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.events.BookCascadeDelete;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({BookServiceImpl.class, BookCascadeDelete.class})
public class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<Author> dbAuthors;
    private List<Genre> dbGenres;
    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Author.class);
        mongoTemplate.dropCollection(Genre.class);
        mongoTemplate.dropCollection(Book.class);
        mongoTemplate.dropCollection(Comment.class);

        dbAuthors = authorRepository.saveAll(List.of(
                new Author("author1","Author_1"),
                new Author("author2","Author_2"),
                new Author("author3","Author_3")));

        dbGenres = genreRepository.saveAll(List.of(
                new Genre("genre1","Genre_1"), new Genre("genre2","Genre_2"), new Genre("genre3","Genre_3"),
                new Genre("genre4","Genre_4"), new Genre("genre5","Genre_5"), new Genre("genre6","Genre_6")));

        dbBooks = bookRepository.saveAll(List.of(
                new Book("book1","BookTitle_1", dbAuthors.get(0), List.of(dbGenres.get(0), dbGenres.get(1))),
                new Book("book2","BookTitle_2", dbAuthors.get(1), List.of(dbGenres.get(2), dbGenres.get(3))),
                new Book("book3","BookTitle_3", dbAuthors.get(2), List.of(dbGenres.get(4), dbGenres.get(5)))));
    }

    @Test
    @DisplayName("findById(): должен корректно находить книгу со всеми данными")
    void findByIdShouldReturnCorrectBook() {
        var expectedBook = dbBooks.get(0);
        var actualBookOpt = bookService.findById(expectedBook.getId());

        assertThat(actualBookOpt).isPresent();
        assertThat(actualBookOpt.get())
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @Test
    @DisplayName("findById(): возвращает empty для несуществующего id")
    void findByIdShouldReturnEmpty() {
        assertThat(bookService.findById("someRandomNonExistentId")).isEmpty();
    }

    @Test
    @DisplayName("findAll(): должен возвращать все книги с авторами и жанрами")
    void findAllShouldReturnAllBooks() {
        List<Book> books = bookService.findAll();
        assertThat(books)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(dbBooks);
    }

    @Test
    @DisplayName("insert(): должен корректно создавать новую книгу")
    void insertShouldCreateBook() {
        var authorForBook = dbAuthors.get(0);
        var genresForBook = List.of(dbGenres.get(0), dbGenres.get(1));
        var genreIds = genresForBook.stream().map(Genre::getId).collect(Collectors.toSet());

        var savedBook = bookService.insert("InsertedTitle", authorForBook.getId(), genreIds);

        // Проверяем, что ID сгенерировался
        assertThat(savedBook.getId()).isNotNull();

        var expectedBook = new Book(savedBook.getId(), "InsertedTitle", authorForBook, genresForBook);

        assertThat(savedBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);

        var reloadedBook = bookService.findById(savedBook.getId()).orElseThrow();
        assertThat(reloadedBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @Test
    @DisplayName("update(): должен корректно обновлять книгу")
    void updateShouldUpdateBook() {
        var bookToUpdate = dbBooks.get(0);
        var newAuthor = dbAuthors.get(1);
        var newGenres = List.of(dbGenres.get(2), dbGenres.get(3));
        Set<String> newGenreIds = newGenres.stream().map(Genre::getId).collect(Collectors.toSet());

        var updatedBook = bookService.update(bookToUpdate.getId(), "UpdatedTitle", newAuthor.getId(), newGenreIds);

        var expectedBook = new Book(bookToUpdate.getId(), "UpdatedTitle", newAuthor, newGenres);

        assertThat(updatedBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);

        var reloadedBook = bookService.findById(bookToUpdate.getId()).orElseThrow();
        assertThat(reloadedBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @Test
    @DisplayName("deleteById(): при удалении книги удаляются её комментарии (проверка слушателя)")
    void deleteByIdShouldCascadeDeleteComments() {
        var bookToDelete = dbBooks.get(0);
        var bookId = bookToDelete.getId();
        commentRepository.save(new Comment("comment1", "Comment 1 for book 1", bookToDelete));
        commentRepository.save(new Comment("comment2", "Comment 2 for book 1", bookToDelete));

        assertThat(commentRepository.findAllByBookId(bookId)).hasSize(2);

        bookRepository.deleteById(bookId);

        assertThat(bookRepository.findById(bookId)).isEmpty();
        assertThat(commentRepository.findAllByBookId(bookId)).isEmpty();
    }
}