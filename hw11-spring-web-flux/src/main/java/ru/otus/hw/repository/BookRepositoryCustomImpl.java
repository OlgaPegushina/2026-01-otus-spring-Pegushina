package ru.otus.hw.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.model.Author;
import ru.otus.hw.model.Book;
import ru.otus.hw.model.Genre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {
    private static final String FIND_ALL_WITH_DETAILS_SQL = """
        SELECT b.id as book_id, b.title, b.author_id,
               a.full_name,
               g.id as genre_id, g.name as genre_name
        FROM books b
        LEFT JOIN authors a ON b.author_id = a.id
        LEFT JOIN books_genres bg ON b.id = bg.book_id
        LEFT JOIN genres g ON bg.genre_id = g.id
    """;

    private static final String FIND_BY_ID_WITH_DETAILS_SQL = FIND_ALL_WITH_DETAILS_SQL + " WHERE b.id = :bookId";

    private final DatabaseClient client;

    @Override
    public Flux<Book> findAllWithDetails() {
        Map<Long, Book> bookMap = new HashMap<>();
        return client.sql(FIND_ALL_WITH_DETAILS_SQL)
                .fetch()
                .all()
                .doOnNext(row -> processRow(bookMap, row))
                .thenMany(Flux.defer(() -> Flux.fromIterable(bookMap.values())));
    }

    @Override
    public Mono<Book> findByIdWithDetails(long bookId) {
        Map<Long, Book> bookMap = new HashMap<>();
        return client.sql(FIND_BY_ID_WITH_DETAILS_SQL)
                .bind("bookId", bookId)
                .fetch()
                .all()
                .doOnNext(row -> processRow(bookMap, row))
                //--then() ждет завершения потока и возвращает Mono<Book> или Mono.empty(), напоминалка
                .then(Mono.defer(() -> Mono.justOrEmpty(bookMap.get(bookId))));
    }

    @Override
    public Mono<Void> deleteGenreLinksByBookId(long bookId) {
        return client.sql("DELETE FROM books_genres WHERE book_id = :bookId")
                .bind("bookId", bookId)
                .then();
    }

    @Override
    public Mono<Void> saveGenreLink(long bookId, long genreId) {
        return client.sql("INSERT INTO books_genres(book_id, genre_id) VALUES (:bookId, :genreId)")
                .bind("bookId", bookId)
                .bind("genreId", genreId)
                .then();
    }

    private void processRow(Map<Long, Book> bookMap, Map<String, Object> row) {
        Long bookId = ((Number) row.get("book_id")).longValue();
        Book book = bookMap.computeIfAbsent(bookId, id -> createBookFromRow(row, id));
        if (row.get("genre_id") != null) {
            Genre genre = new Genre(
                    ((Number) row.get("genre_id")).longValue(),
                    (String) row.get("genre_name")
            );

            if (book.getGenres().stream().noneMatch(g -> g.getId() == genre.getId())) {
                book.getGenres().add(genre);
            }
        }
    }

    private Book createBookFromRow(Map<String, Object> row, Long bookId) {
        Author author = new Author(
                ((Number) row.get("author_id")).longValue(),
                (String) row.get("full_name")
        );
        return new Book(
                bookId,
                (String) row.get("title"),
                author.getId(),
                author,
                new ArrayList<>()
        );
    }
}