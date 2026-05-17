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
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
class JpaBookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findById(): должен возвращать книгу по id и подгружать author+genres (fetchgraph book-author-genres)")
    void shouldFindBookByIdWithAuthorAndGenres() {
        var expected = em.find(Book.class, 1L);
        assertThat(expected).isNotNull();

        // -- инициализируем lazy поля для expected (для корректного сравнения)
        expected.getAuthor().getFullName();
        expected.getGenres().size();

        em.clear();

        var actualOpt = bookRepository.findById(1L);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.get();

        // -- проверяем, что entity graph реально подгрузил связи
        assertThat(Hibernate.isInitialized(actual.getAuthor())).isTrue();
        assertThat(Hibernate.isInitialized(actual.getGenres())).isTrue();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("findById(): должен возвращать empty для несуществующего id")
    void shouldReturnEmptyIfBookNotFound() {
        em.clear();

        var actual = bookRepository.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("save(): должен сохранять новую книгу (persist), назначать id и связи genres")
    void shouldInsertNewBook() {
        var author = em.find(Author.class, 1L);
        var genres = List.of(em.find(Genre.class, 1L), em.find(Genre.class, 2L));

        assertThat(author).isNotNull();
        assertThat(genres).doesNotContainNull();

        em.clear();

        var book = new Book(0L, "NewBookTitle", author, genres);

        var saved = bookRepository.save(book);
        em.flush();
        em.clear();

        assertThat(saved.getId()).isPositive();

        var fromDb = em.find(Book.class, saved.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getTitle()).isEqualTo("NewBookTitle");
        assertThat(fromDb.getAuthor().getId()).isEqualTo(1L);

        fromDb.getGenres().size();
        assertThat(fromDb.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("save(): должен обновлять существующую книгу (merge)")
    void shouldUpdateExistingBook() {
        var existing = em.find(Book.class, 2L);
        assertThat(existing).isNotNull();

        em.detach(existing);
        existing.setTitle("UpdatedTitle");

        var saved = bookRepository.save(existing);
        em.flush();
        em.clear();

        var fromDb = em.find(Book.class, 2L);
        assertThat(fromDb.getTitle()).isEqualTo("UpdatedTitle");
        assertThat(saved.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("deleteById(): должен удалять книгу по id")
    void shouldDeleteBookById() {
        assertThat(em.find(Book.class, 1L)).isNotNull();

        em.clear();

        bookRepository.deleteById(1L);
        em.flush();
        em.clear();

        assertThat(em.find(Book.class, 1L)).isNull();
    }

    @Test
    @DisplayName("deleteById(): не должен падать, если книги с таким id нет")
    void shouldNotFailWhenDeletingNonExistingBook() {
        em.clear();

        bookRepository.deleteById(999L);
        em.flush();
        em.clear();

        assertThat(em.find(Book.class, 999L)).isNull();
    }

    @ParameterizedTest(name = "bookId={0}: title/author/genres должны соответствовать data.sql")
    @MethodSource("booksFromDataSql")
    @DisplayName("findById(): должен возвращать author+genres для книг из data.sql")
    void shouldReturnCorrectAuthorAndGenresForBooksFromDataSql(long bookId,
                                                               String expectedTitle,
                                                               long expectedAuthorId,
                                                               String expectedAuthorName,
                                                               String expectedGenreIdsCsv) {
        em.clear();

        var actualOpt = bookRepository.findById(bookId);
        assertThat(actualOpt).isPresent();

        var book = actualOpt.get();

        // -- graph должен подгрузить оба поля
        assertThat(Hibernate.isInitialized(book.getAuthor())).isTrue();
        assertThat(Hibernate.isInitialized(book.getGenres())).isTrue();

        assertThat(book.getTitle()).isEqualTo(expectedTitle);

        assertThat(book.getAuthor().getId()).isEqualTo(expectedAuthorId);
        assertThat(book.getAuthor().getFullName()).isEqualTo(expectedAuthorName);

        var expectedGenreIds = Arrays.stream(expectedGenreIdsCsv.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();

        assertThat(book.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrderElementsOf(expectedGenreIds);
    }

    static Stream<Arguments> booksFromDataSql() {
        return Stream.of(
                Arguments.of(1L, "BookTitle_1", 1L, "Author_1", "1,2"),
                Arguments.of(2L, "BookTitle_2", 2L, "Author_2", "3,4"),
                Arguments.of(3L, "BookTitle_3", 3L, "Author_3", "5,6")
        );
    }

    @Test
    @DisplayName("findAll(): должен возвращать все книги и подгружать author (fetchgraph book-author), но не genres")
    void shouldReturnAllBooksWithAuthorsOnly() {
        em.clear();

        var actual = bookRepository.findAll();

        assertThat(actual).hasSize(3);

        // author подгружен entity graph
        assertThat(actual).allMatch(b -> Hibernate.isInitialized(b.getAuthor()));

        // жанры в findAll() не подгружаем
        assertThat(actual).allMatch(b -> !Hibernate.isInitialized(b.getGenres()));

        // проверяем набор книг по полям (без жанров)
        assertThat(actual)
                .extracting(
                        Book::getId,
                        Book::getTitle,
                        b -> b.getAuthor().getId(),
                        b -> b.getAuthor().getFullName()
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "BookTitle_1", 1L, "Author_1"),
                        tuple(2L, "BookTitle_2", 2L, "Author_2"),
                        tuple(3L, "BookTitle_3", 3L, "Author_3")
                );
    }
}