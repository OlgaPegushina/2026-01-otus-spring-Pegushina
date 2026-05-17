package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataMongoTest
@Import({CommentServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentServiceImplTest {
    @Autowired
    private CommentService commentService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<Book> dbBooks;
    private List<Comment> dbComments;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Book.class);
        mongoTemplate.dropCollection(Comment.class);
        mongoTemplate.dropCollection(Author.class);
        mongoTemplate.dropCollection(Genre.class);

        var author = mongoTemplate.save(new Author("author1","Author_1"));
        var genre = mongoTemplate.save(new Genre("genre1","Genre_1"));
        dbBooks = bookRepository.saveAll(List.of(
                new Book("book1","BookTitle_1", author, List.of(genre)),
                new Book("book2","BookTitle_2", author, List.of(genre))
        ));

        dbComments = commentRepository.saveAll(List.of(
                new Comment("comment1","Comment_1", dbBooks.get(0)),
                new Comment("comment2","Comment_2", dbBooks.get(0))
        ));
    }

    @Test
    @DisplayName("findById(): должен вернуть комментарий по id")
    void findByIdShouldReturnComment() {
        var expectedComment = dbComments.get(0);
        var actualCommentOpt = commentService.findById(expectedComment.getId());

        assertThat(actualCommentOpt).isPresent();
        assertThat(actualCommentOpt.get()).isEqualTo(expectedComment);
    }

    @Test
    @DisplayName("findById(): должен вернуть empty для несуществующего id")
    void findByIdShouldReturnEmpty() {
        assertThat(commentService.findById("nonExistentId")).isEmpty();
    }

    @Test
    @DisplayName("findAllByBookId(): должен вернуть список комментариев книги")
    void findAllByBookIdShouldReturnComments() {
        var bookWithComments = dbBooks.get(0);
        var actualComments = commentService.findAllByBookId(bookWithComments.getId());

        assertThat(actualComments).containsExactlyInAnyOrderElementsOf(dbComments);
    }

    @Test
    @DisplayName("insert(): должен сохранить комментарий для существующей книги")
    void insertShouldSaveComment() {
        var bookForComment = dbBooks.get(0);
        var savedComment = commentService.insert("NewComment", bookForComment.getId());

        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("NewComment");
        assertThat(savedComment.getBook()).isEqualTo(bookForComment);

        var fromDb = commentRepository.findById(savedComment.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get()).isEqualTo(savedComment);
    }

    @Test
    @DisplayName("insert(): должен бросить EntityNotFoundException если книга не найдена")
    void insertShouldThrowIfBookNotFound() {
        assertThatThrownBy(() -> commentService.insert("X", "999"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("update(): должен обновить текст существующего комментария")
    void updateShouldUpdateComment() {
        var commentToUpdate = dbComments.get(0);
        var expectedBook = dbBooks.get(0);
        var expectedComment = new Comment(commentToUpdate.getId(), "UpdatedText", expectedBook);

        var updatedComment = commentService.update(commentToUpdate.getId(), "UpdatedText");

        assertThat(updatedComment)
                .usingRecursiveComparison()
                // Вот он, твой метод! Идеально подходит.
                .ignoringFieldsMatchingRegexes(".*CGLIB\\$.*")
                .isEqualTo(expectedComment);

        var fromDb = commentRepository.findById(commentToUpdate.getId()).orElseThrow();
        assertThat(fromDb.getText()).isEqualTo("UpdatedText");
        assertThat(fromDb.getBook().getId()).isEqualTo(expectedBook.getId());
    }

    @Test
    @DisplayName("update(): должен бросить EntityNotFoundException, если комментарий не найден")
    void updateShouldThrowIfCommentNotFound() {
        var nonExistentCommentId = "999";
        assertThatThrownBy(() -> commentService.update(nonExistentCommentId, "UpdatedText"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id %s not found".formatted(nonExistentCommentId));
    }

    @Test
    @DisplayName("deleteById(): должен удалить существующий комментарий")
    void deleteByIdShouldDeleteComment() {
        var commentToDelete = dbComments.get(1);

        assertThat(commentRepository.findById(commentToDelete.getId())).isPresent();

        commentService.deleteById(commentToDelete.getId());

        assertThat(commentRepository.findById(commentToDelete.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteById(): не должен падать при удалении несуществующего комментария")
    void deleteByIdShouldNotFailIfNotExists() {
        assertThatCode(() -> commentService.deleteById("nonExistentCommentId"))
                .doesNotThrowAnyException();
    }
}
