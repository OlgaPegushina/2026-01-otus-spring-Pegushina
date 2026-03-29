package ru.otus.hw.repositories;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@Import(JpaCommentRepository.class)
class JpaCommentRepositoryTest {

    @Autowired
    private JpaCommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findById(): должен возвращать comment по id (book остается LAZY)")
    void shouldFindCommentById() {
        em.clear();

        var actualOpt = commentRepository.findById(1L);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.get();

        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getText()).isEqualTo("Comment_1");

        // ManyToOne(fetch = LAZY) — не должен подгружаться сам
        assertThat(Hibernate.isInitialized(actual.getBook())).isFalse();

        // но id книги можем проверить (proxy отдаёт id без инициализ.)
        assertThat(actual.getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById(): должен возвращать empty для несуществующего id")
    void shouldReturnEmptyIfCommentNotFound() {
        em.clear();

        var actual = commentRepository.findById(999L);

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest(name = "bookId={0} -> ожидаемые комменты: {1}")
    @MethodSource("commentsByBookFromDataSql")
    @DisplayName("findAllByBookId(): должен возвращать все комментарии для книги")
    void shouldFindAllCommentsByBookId(long bookId, String expectedTextsCsv, int expectedSize) {
        em.clear();

        var actual = commentRepository.findAllByBookId(bookId);

        assertThat(actual).hasSize(expectedSize);

        // book не должен быть инициализирован
        assertThat(actual).allMatch(c -> !Hibernate.isInitialized(c.getBook()));

        // проверяем, что все комменты реально относятся к нужной книге
        assertThat(actual).allMatch(c -> c.getBook().getId() == bookId);

        // сравнение по текстам (без требований к порядку)
        if (expectedSize == 0) {
            assertThat(actual).isEmpty();
            return;
        }

        var expectedTexts = Stream.of(expectedTextsCsv.split(","))
                .map(String::trim)
                .toList();

        assertThat(actual)
                .extracting(Comment::getText)
                .containsExactlyInAnyOrderElementsOf(expectedTexts);
    }

    static Stream<Arguments> commentsByBookFromDataSql() {
        return Stream.of(
                Arguments.of(1L, "Comment_1, Comment_2", 2),
                Arguments.of(2L, "", 0),
                Arguments.of(3L, "Comment_3", 1)
        );
    }

    @Test
    @DisplayName("save(): должен сохранять новый comment (persist), назначать id")
    void shouldInsertNewComment() {
        em.clear();

        // getReference() не загружает все поля книги сразу.
        // он возвращает прокси-объект, который будет загружать данные только при первом обращении к его полям.
        var bookRef = em.getEntityManager().getReference(Book.class, 1L);

        var comment = new Comment(0L, "NewComment", bookRef);

        var saved = commentRepository.save(comment);
        em.flush();
        em.clear();

        assertThat(saved.getId()).isPositive();

        var fromDb = em.find(Comment.class, saved.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getText()).isEqualTo("NewComment");
        assertThat(fromDb.getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("save(): должен обновлять существующий comment (merge)")
    void shouldUpdateExistingComment() {
        var existing = em.find(Comment.class, 2L);
        assertThat(existing).isNotNull();

        em.detach(existing);
        existing.setText("UpdatedComment_2");

        var saved = commentRepository.save(existing);
        em.flush();
        em.clear();

        assertThat(saved.getId()).isEqualTo(2L);

        var fromDb = em.find(Comment.class, 2L);
        assertThat(fromDb.getText()).isEqualTo("UpdatedComment_2");
    }

    @Test
    @DisplayName("deleteById(): должен удалять comment по id")
    void shouldDeleteCommentById() {
        assertThat(em.find(Comment.class, 1L)).isNotNull();

        em.clear();

        commentRepository.deleteById(1L);
        em.flush();
        em.clear();

        assertThat(em.find(Comment.class, 1L)).isNull();
    }

    @Test
    @DisplayName("deleteById(): не должен падать, если comment с таким id нет")
    void shouldNotFailWhenDeletingNonExistingComment() {
        em.clear();

        commentRepository.deleteById(999L);
        em.flush();
        em.clear();

        assertThat(em.find(Comment.class, 999L)).isNull();
    }

    @Test
    @DisplayName("findAllByBookId(): проверить набор (id,text,bookId) через tuple()")
    void shouldReturnCommentsAsTuplesForBook1() {
        em.clear();

        var actual = commentRepository.findAllByBookId(1L);

        assertThat(actual)
                .extracting(
                        Comment::getId,
                        Comment::getText,
                        c -> c.getBook().getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "Comment_1", 1L),
                        tuple(2L, "Comment_2", 1L)
                );
    }
}
