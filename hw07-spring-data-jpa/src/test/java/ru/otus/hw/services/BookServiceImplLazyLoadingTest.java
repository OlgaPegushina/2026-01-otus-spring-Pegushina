package ru.otus.hw.services;

import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
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
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import({
        BookServiceImpl.class,
})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // тест не в транзакции
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // чтобы insert/update/delete не ломали следующие тесты
public class BookServiceImplLazyLoadingTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentRepository commentRepository;

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
    @DisplayName("findAll(): author подгружается, genres не подгружаются (lazy) снаружи сервиса")
    void findAllShouldNotThrowLazyInitialization() {
        var books = bookService.findAll();
        assertThat(books).hasSize(3);

        // author должен быть подгружен
        assertThatCode(() -> books.forEach(b -> b.getAuthor().getFullName()))
                .doesNotThrowAnyException();

        // genres не должны быть подгружены
        assertThat(books).allSatisfy(b ->
                assertThat(Hibernate.isInitialized(b.getGenres())).isFalse()
        );

        // при попытке доступа к genres должны упасть
        assertThatThrownBy(() -> books.forEach(b -> b.getGenres().size()))
                .isInstanceOf(LazyInitializationException.class);
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
}