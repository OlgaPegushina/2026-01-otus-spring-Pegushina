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
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
        var bookOpt = bookService.findById(1L);
        assertThat(bookOpt).isPresent();

        var book = bookOpt.get();

        assertThatCode(() -> book.getAuthor().getFullName())
                .doesNotThrowAnyException();

        assertThatCode(() -> book.getGenres().size())
                .doesNotThrowAnyException();

        // контроль, что данные адекватные (data.sql)
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");
        assertThat(book.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(book.getGenres()).hasSize(2);
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
        var saved = bookService.insert("InsertedTitle", 1L, Set.of(1L, 2L));

        assertThat(saved.getId()).isPositive();

        assertThatCode(() -> saved.getAuthor().getFullName())
                .doesNotThrowAnyException();

        assertThatCode(() -> saved.getGenres().size())
                .doesNotThrowAnyException();

        // доп. проверка: через сервис и тоже без LazyInitializationException
        var reloaded = bookService.findById(saved.getId()).orElseThrow();
        assertThatCode(() -> reloaded.getAuthor().getFullName()).doesNotThrowAnyException();
        assertThatCode(() -> reloaded.getGenres().size()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("update(): возвращаемая книга снаружи сервиса даёт читать author и genres без LazyInitializationException")
    void updateShouldNotThrowLazyInitialization() {
        var updated = bookService.update(1L, "UpdatedTitle", 2L, Set.of(3L, 4L));

        assertThat(updated.getId()).isEqualTo(1L);

        assertThatCode(() -> updated.getAuthor().getFullName())
                .doesNotThrowAnyException();

        assertThatCode(() -> updated.getGenres().size())
                .doesNotThrowAnyException();

        var reloaded = bookService.findById(1L).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("UpdatedTitle");
        assertThat(reloaded.getAuthor().getId()).isEqualTo(2L);
        assertThat(reloaded.getGenres()).hasSize(2);
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