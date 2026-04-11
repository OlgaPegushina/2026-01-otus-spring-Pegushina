package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import({
        CommentServiceImpl.class,
})
class CommentServiceImplTest {
    private static final long FIRST_BOOK_ID = 1L;
    private static final long FIRST_COMMENT_ID = 1L;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findById(): должен вернуть комментарий по id")
    void findByIdShouldReturnComment() {
        var expectedBook = em.find(Book.class, FIRST_BOOK_ID);
        var expectedComment = new Comment(FIRST_COMMENT_ID, "Comment_1", expectedBook);

        var actualCommentOpt = commentService.findById(FIRST_COMMENT_ID);

        assertThat(actualCommentOpt).isPresent();

        assertThat(actualCommentOpt.get())
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @Test
    @DisplayName("findById(): должен вернуть empty для несуществующего id")
    void findByIdShouldReturnEmpty() {
        assertThat(commentService.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("findAllByBookId(): должен вернуть список комментариев книги")
    void findAllByBookIdShouldReturnComments() {
        var expectedBook = em.find(Book.class, FIRST_BOOK_ID);
        var expectedComments = List.of(
                new Comment(1L, "Comment_1", expectedBook),
                new Comment(2L, "Comment_2", expectedBook)
        );

        var actualComments = commentService.findAllByBookId(FIRST_BOOK_ID);

        assertThat(actualComments)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedComments);
    }

    @Test
    @DisplayName("insert(): должен сохранить комментарий для существующей книги")
    void insertShouldSaveComment() {
        var bookForComment = em.find(Book.class, FIRST_BOOK_ID);

        var savedComment = commentService.insert("NewComment", FIRST_BOOK_ID);

        var expectedComment = new Comment(savedComment.getId(), "NewComment", bookForComment);

        assertThat(savedComment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);

        em.flush();
        em.clear();

        var fromDb = em.find(Comment.class, savedComment.getId());

        // объект вообще нашелся
        assertThat(fromDb).isNotNull();

        // Проверяем поля самого комментария
        assertThat(fromDb.getId()).isEqualTo(savedComment.getId());
        assertThat(fromDb.getText()).isEqualTo("NewComment");

        // Проверяем, что комментарий все еще связан с правильной книгой, сравнивая ID
        assertThat(fromDb.getBook()).isNotNull();
        assertThat(fromDb.getBook().getId()).isEqualTo(FIRST_BOOK_ID);
    }

    @Test
    @DisplayName("insert(): должен бросить EntityNotFoundException если книга не найдена")
    void insertShouldThrowIfBookNotFound() {
        assertThatThrownBy(() -> commentService.insert("X", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("update(): должен обновить текст существующего комментария")
    void updateShouldUpdateComment() {
        var originalBook = em.find(Book.class, FIRST_BOOK_ID);
        var expectedComment = new Comment(FIRST_COMMENT_ID, "UpdatedText", originalBook);

        var updatedComment = commentService.update(FIRST_COMMENT_ID, "UpdatedText");

        assertThat(updatedComment)
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);

        String expectedText = "UpdatedText";

        em.flush();
        em.clear();

        var fromDb = em.find(Comment.class, FIRST_COMMENT_ID);

        // объект вообще нашелся
        assertThat(fromDb).isNotNull();

        // Проверяем поля самого комментария
        assertThat(fromDb.getId()).isEqualTo(FIRST_COMMENT_ID);
        assertThat(fromDb.getText()).isEqualTo(expectedText);

        // Проверяем, что комментарий все еще связан с правильной книгой, сравнивая ID
        assertThat(fromDb.getBook()).isNotNull();
        assertThat(fromDb.getBook().getId()).isEqualTo(FIRST_BOOK_ID);
    }

    @Test
    @DisplayName("update(): должен бросить EntityNotFoundException, если комментарий не найден")
    void updateShouldThrowIfCommentNotFound() {
        long nonExistentCommentId = 999L;

        assertThatThrownBy(() -> commentService.update(nonExistentCommentId, "UpdatedText"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id %d not found".formatted(nonExistentCommentId));
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
