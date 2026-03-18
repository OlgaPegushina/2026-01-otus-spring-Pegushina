package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcOperations namedJdbc;

    @Override
    public Optional<Book> findById(long id) {
        var sql = """
                select
                    b.id as book_id,
                    b.title as book_title,
                    a.id as author_id,
                    a.full_name as author_full_name,
                    g.id as genre_id,
                    g.name as genre_name
                from books b
                join authors a on a.id = b.author_id
                left join books_genres bg on bg.book_id = b.id
                left join genres g on g.id = bg.genre_id
                where b.id = :id
                order by g.id
                """;

        var book = namedJdbc.query(sql, Map.of("id", id), new BookResultSetExtractor());
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        namedJdbc.update("delete from books where id = :id", Map.of("id", id));
    }

    private List<Book> getAllBooksWithoutGenres() {
        var sql = """
                select
                    b.id as book_id,
                    b.title as book_title,
                    a.id as author_id,
                    a.full_name as author_full_name
                from books b
                join authors a on a.id = b.author_id
                order by b.id
                """;

        return namedJdbc.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        var sql = """
                select book_id, genre_id
                from books_genres
                order by book_id, genre_id
                """;
        return namedJdbc.query(sql, new BookGenreRelationRowMapper());
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, Book> booksMap = booksWithoutGenres.stream()
                .collect(Collectors.toMap(Book::getId, book -> book));
        Map<Long, Genre> genresMap = genres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));
        for (var rel : relations) {
            var book = booksMap.get(rel.bookId());
            var genre = genresMap.get(rel.genreId());
            if (book != null && genre != null) {
                book.getGenres().add(genre);
            }
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        var sql = """
                insert into books (title, author_id)
                values (:title, :authorId)
                """;

        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId());

        namedJdbc.update(sql, params, keyHolder, new String[]{"id"});

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));

        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var sql = """
                update books
                set title = :title,
                    author_id = :authorId
                where id = :id
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", book.getId())
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId());

        var updated = namedJdbc.update(sql, params);

        if (updated == 0) {
            throw new EntityNotFoundException("Book with id= %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var genres = book.getGenres();
        if (genres == null || genres.isEmpty()) {
            return;
        }

        var sql = """
                insert into books_genres (book_id, genre_id)
                values (:bookId, :genreId)
                """;

        var batch = genres.stream()
                .map(g -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId())
                        .addValue("genreId", g.getId()))
                .toArray(MapSqlParameterSource[]::new);

        namedJdbc.batchUpdate(sql, batch);
    }

    private void removeGenresRelationsFor(Book book) {
        namedJdbc.update(
                "delete from books_genres where book_id = :bookId",
                Map.of("bookId", book.getId())
        );
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            var author = new Author(
                    rs.getLong("author_id"),
                    rs.getString("author_full_name")
            );

            return new Book(
                    rs.getLong("book_id"),
                    rs.getString("book_title"),
                    author,
                    new ArrayList<>()
            );
        }
    }

    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                return null;
            }

            var author = new Author(rs.getLong("author_id"), rs.getString("author_full_name"));
            var book = new Book(rs.getLong("book_id"), rs.getString("book_title"), author, new ArrayList<>());

             do {
                 Long genreId = (Long) rs.getObject("genre_id");
                 if (genreId != null) {
                     Genre genre = new Genre(genreId, rs.getString("genre_name"));
                     book.getGenres().add(genre);
                 }
            } while (rs.next());

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }

    private static class BookGenreRelationRowMapper implements RowMapper<BookGenreRelation> {

        @Override
        public BookGenreRelation mapRow(ResultSet rs, int i) throws SQLException {
            long bookId = rs.getLong("book_id");
            long genreId = rs.getLong("genre_id");
            return new BookGenreRelation(bookId, genreId);
        }
    }
}
