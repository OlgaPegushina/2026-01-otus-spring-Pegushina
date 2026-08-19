package ru.otus.hw;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import ru.otus.hw.domain.jpa.BookEntity;
import ru.otus.hw.domain.jpa.GenreEntity;
import ru.otus.hw.domain.mongo.AuthorDoc;
import ru.otus.hw.domain.mongo.BookDoc;
import ru.otus.hw.domain.mongo.GenreDoc;

@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        "spring.jpa.defer-datasource-initialization=true",

        "spring.sql.init.mode=embedded"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MigrateJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("migrateJob")
    private Job migrateJob;

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate tx;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        ensureCollectionExists("authors");
        ensureCollectionExists("genres");
        ensureCollectionExists("books");
    }

    private void ensureCollectionExists(String name) {
        if (!mongoTemplate.collectionExists(name)) {
            mongoTemplate.createCollection(name);
        }
    }

    private long countJpa(String jpql) {
        Long result = tx.execute(status ->
                em.createQuery(jpql, Long.class).getSingleResult()
        );
        return Objects.requireNonNull(result, "Count query returned null");
    }

    private Map<String, ExpectedBook> expectedBooksByTitleFromJpa() {
        return tx.execute(status -> {
            List<BookEntity> books = em.createQuery(
                            "select distinct b from BookEntity b " +
                            "join fetch b.author a " +
                            "left join fetch b.genres g",
                            BookEntity.class
                    )
                    .getResultList();

            Map<String, ExpectedBook> result = new HashMap<>();
            for (BookEntity b : books) {
                String title = b.getTitle();
                String authorName = b.getAuthor().getFullName();
                Set<String> genreNames = b.getGenres().stream()
                        .map(GenreEntity::getName)
                        .collect(Collectors.toSet());

                result.put(title, new ExpectedBook(title, authorName, genreNames));
            }
            return result;
        });
    }

    @Test
    void shouldMigrateAndKeepLinksForAnyDataSql() throws Exception {
        long authorsExpected = countJpa("select count(a) from AuthorEntity a");
        long genresExpected  = countJpa("select count(g) from GenreEntity g");
        long booksExpected   = countJpa("select count(b) from BookEntity b");

        Map<String, ExpectedBook> expectedBooksByTitle = expectedBooksByTitleFromJpa();
        assertThat((long) expectedBooksByTitle.size()).isEqualTo(booksExpected);

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(migrateJob, params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        Map<String, StepExecution> steps = execution.getStepExecutions().stream()
                .collect(Collectors.toMap(
                        StepExecution::getStepName,
                        Function.identity(),
                        (a, b) -> a
                ));

        assertThat(steps.keySet()).contains("cleanupStep", "authorsStep", "genresStep", "booksStep");

        assertThat(steps.get("authorsStep").getReadCount()).isEqualTo(authorsExpected);
        assertThat(steps.get("authorsStep").getWriteCount()).isEqualTo(authorsExpected);

        assertThat(steps.get("genresStep").getReadCount()).isEqualTo(genresExpected);
        assertThat(steps.get("genresStep").getWriteCount()).isEqualTo(genresExpected);

        assertThat(steps.get("booksStep").getReadCount()).isEqualTo(booksExpected);
        assertThat(steps.get("booksStep").getWriteCount()).isEqualTo(booksExpected);

        List<AuthorDoc> authors = mongoTemplate.findAll(AuthorDoc.class, "authors");
        List<GenreDoc> genres = mongoTemplate.findAll(GenreDoc.class, "genres");
        List<BookDoc> books = mongoTemplate.findAll(BookDoc.class, "books");

        assertThat((long) authors.size()).isEqualTo(authorsExpected);
        assertThat((long) genres.size()).isEqualTo(genresExpected);
        assertThat((long) books.size()).isEqualTo(booksExpected);

        for (BookDoc bookDoc : books) {
            String title = bookDoc.getTitle();
            assertThat(title).isNotBlank();

            ExpectedBook expected = expectedBooksByTitle.get(title);
            assertThat(expected)
                    .as("Для книги '%s' должна быть ожидаемая запись из JPA (по title)", title)
                    .isNotNull();

            assertThat(bookDoc.getAuthorId()).isNotBlank();
            AuthorDoc authorDoc = mongoTemplate.findById(bookDoc.getAuthorId(), AuthorDoc.class, "authors");
            assertThat(authorDoc).isNotNull();
            assertThat(authorDoc.getFullName()).isEqualTo(expected.authorFullName());

            assertThat(bookDoc.getGenreIds()).isNotNull();
            assertThat(bookDoc.getGenreIds()).isNotEmpty();

            List<GenreDoc> genreDocs = bookDoc.getGenreIds().stream()
                    .map(id -> mongoTemplate.findById(id, GenreDoc.class, "genres"))
                    .toList();

            assertThat(genreDocs).allMatch(Objects::nonNull);

            Set<String> actualGenreNames = genreDocs.stream()
                    .map(GenreDoc::getName)
                    .collect(Collectors.toSet());

            assertThat(actualGenreNames).isEqualTo(expected.genreNames());
        }
    }

    private record ExpectedBook(String title, String authorFullName, Set<String> genreNames) {}
}