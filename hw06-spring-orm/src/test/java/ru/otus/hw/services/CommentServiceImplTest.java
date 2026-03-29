package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import({
        CommentServiceImpl.class,
        JpaCommentRepository.class,
        JpaBookRepository.class
})
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findById(): должен вернуть комментарий по id")
    void findByIdShouldReturnComment() {
        var actual = commentService.findById(1L);

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(1L);
        assertThat(actual.get().getText()).isEqualTo("Comment_1");
        // book LAZY, но id доступен без инициализации
        assertThat(actual.get().getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById(): должен вернуть empty для несуществующего id")
    void findByIdShouldReturnEmpty() {
        assertThat(commentService.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("findAllByBookId(): должен вернуть список комментариев книги")
    void findAllByBookIdShouldReturnComments() {
        var actual = commentService.findAllByBookId(1L);

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting(Comment::getText)
                .containsExactlyInAnyOrder("Comment_1", "Comment_2");
    }

    @Test
    @DisplayName("insert(): должен сохранить комментарий для существующей книги")
    void insertShouldSaveComment() {
        var saved = commentService.insert("NewComment", 1L);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getText()).isEqualTo("NewComment");
        assertThat(saved.getBook().getId()).isEqualTo(1L);

        em.flush();
        em.clear();

        var fromDb = em.find(Comment.class, saved.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getText()).isEqualTo("NewComment");
        assertThat(fromDb.getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("insert(): должен бросить EntityNotFoundException если книга не найдена")
    void insertShouldThrowIfBookNotFound() {
        assertThatThrownBy(() -> commentService.insert("X", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("update(): должен обновить текст и книгу у существующего комментария")
    void updateShouldUpdateComment() {
        // comment 1 из data.sql: ("Comment_1", book 1)
        var updated = commentService.update(1L, "UpdatedText", 3L);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getText()).isEqualTo("UpdatedText");
        assertThat(updated.getBook().getId()).isEqualTo(3L);

        em.flush();
        em.clear();

        var fromDb = em.find(Comment.class, 1L);
        assertThat(fromDb.getText()).isEqualTo("UpdatedText");
        assertThat(fromDb.getBook().getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("update(): должен бросить EntityNotFoundException если книга не найдена")
    void updateShouldThrowIfBookNotFound() {
        assertThatThrownBy(() -> commentService.update(1L, "UpdatedText", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("deleteById(): должен удалить существующий комментарий")
    void deleteByIdShouldDeleteComment() {
        assertThat(em.find(Comment.class, 2L)).isNotNull();

        commentService.deleteById(2L);

        em.flush();
        em.clear();

        assertThat(em.find(Comment.class, 2L)).isNull();
        assertThat(commentService.findAllByBookId(1L))
                .extracting(Comment::getText)
                .containsExactlyInAnyOrder("Comment_1");
    }

    @Test
    @DisplayName("deleteById(): не должен падать при удалении несуществующего комментария")
    void deleteByIdShouldNotFailIfNotExists() {
        assertThatCode(() -> commentService.deleteById(999L))
                .doesNotThrowAnyException();
    }
}
