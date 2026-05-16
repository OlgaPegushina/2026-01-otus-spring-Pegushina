package ru.otus.hw.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        BookServiceImpl.class,
        JpaBookRepository.class,
        JpaAuthorRepository.class,
        JpaGenreRepository.class,
        JpaCommentRepository.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // тест не в транзакции
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // чтобы insert/update/delete не ломали следующие тесты
public class BookServiceImplLazyLoadingTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private JpaCommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("findById(): снаружи сервиса можно читать author и genres без LazyInitializationException")
    void findByIdShouldNotThrowLazyInitialization() {
        var expectedAuthor = new Author(1L, "Author_1");
        var expectedGenres = List.of(
                new Genre(1L, "Genre_1"),
                new Genre(2L, "Genre_2")
        );
        var expectedBook = new Book(1L, "BookTitle_1", expectedAuthor, expectedGenres);

        var actualBookOpt = bookService.findById(1L);

        assertThat(actualBookOpt).isPresent();
        var actualBook = actualBookOpt.get();

        assertThat(actualBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @Test
    @DisplayName("findById(): возвращает empty для несуществующего id")
    void findByIdShouldReturnEmpty() {
        assertThat(bookService.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("findAll(): должен возвращать книги с инициализированными жанрами и автором")
    void findAllShouldNotThrowLazyInitialization() {

        List<Book> books = bookService.findAll();
        List<Book> expectedBooks = getDbBooks();
        assertThat(books)
                .usingRecursiveComparison()
                .isEqualTo(expectedBooks);
    }

    @Test
    @DisplayName("insert(): возвращаемая книга снаружи сервиса даёт читать author и genres без LazyInitializationException")
    void insertShouldNotThrowLazyInitialization() {
        var authorForBook = new Author(1L, "Author_1");
        var genresForBook = List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2"));
        var genresIds = genresForBook.stream().map(Genre::getId).collect(Collectors.toSet());

        var savedBook = bookService.insert("InsertedTitle", authorForBook.getId(), genresIds);

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
    @DisplayName("update(): возвращаемая книга снаружи сервиса даёт читать author и genres без LazyInitializationException")
    void updateShouldNotThrowLazyInitialization() {
        var expectedAuthor = new Author(2L, "Author_2");
        var expectedGenres = List.of(
                new Genre(3L, "Genre_3"),
                new Genre(4L, "Genre_4")
        );
        var expectedBook = new Book(1L, "UpdatedTitle", expectedAuthor, expectedGenres);

        var genresIds = expectedGenres.stream().map(Genre::getId).collect(Collectors.toSet());

        var updatedBook = bookService.update(1L, "UpdatedTitle", expectedAuthor.getId(), genresIds);

        assertThat(updatedBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);

        var reloadedBook = bookService.findById(1L).orElseThrow();
        assertThat(reloadedBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @Test
    @DisplayName("deleteById(): при удалении книги удаляются её комментарии (cascade)")
    void deleteByIdShouldCascadeDeleteComments() {
        long bookId = 1L;

        // у книги есть комментарии
        assertThat(commentRepository.findAllByBookId(bookId)).hasSize(2);

        // общее количество комментариев через EntityManager
        long totalBefore = em.createQuery("select count(c) from Comment c", Long.class)
                .getSingleResult();
        assertThat(totalBefore).isEqualTo(3L);

        // удаляем книгу
        bookService.deleteById(bookId);

        // на всякий: если где-то остался persistence context
        em.clear();

        // книга удалена
        assertThat(bookService.findById(bookId)).isEmpty();

        // проверка: комментарии книги удалены
        assertThat(commentRepository.findAllByBookId(bookId)).isEmpty();

        // всего комментариев стало меньше
        long totalAfter = em.createQuery("select count(c) from Comment c", Long.class)
                .getSingleResult();
        assertThat(totalAfter).isEqualTo(1L);
    }

    private List<Book> getDbBooks() {

        var author1 = new Author(1L, "Author_1");
        var author2 = new Author(2L, "Author_2");
        var author3 = new Author(3L, "Author_3");

        var genre1 = new Genre(1L, "Genre_1");
        var genre2 = new Genre(2L, "Genre_2");
        var genre3 = new Genre(3L, "Genre_3");
        var genre4 = new Genre(4L, "Genre_4");
        var genre5 = new Genre(5L, "Genre_5");
        var genre6 = new Genre(6L, "Genre_6");

        var book1 = new Book(1L, "BookTitle_1", author1, List.of(genre1, genre2));
        var book2 = new Book(2L, "BookTitle_2", author2, List.of(genre3, genre4));
        var book3 = new Book(3L, "BookTitle_3", author3, List.of(genre5, genre6));

        return List.of(book1, book2, book3);
    }
}