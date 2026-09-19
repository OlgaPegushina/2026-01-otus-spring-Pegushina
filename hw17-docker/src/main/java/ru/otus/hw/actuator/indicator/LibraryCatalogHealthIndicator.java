package ru.otus.hw.actuator.indicator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.GenreRepository;

@Component
@RequiredArgsConstructor
public class LibraryCatalogHealthIndicator extends AbstractHealthIndicator {

    private static final Status DEGRADED = new Status("DEGRADED");

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    protected void doHealthCheck(Health.Builder builder) {

        //--убрала try catch, так как эта обработка делается в родительском классе

        long books = bookRepository.count();
        long authors = authorRepository.count();
        long genres = genreRepository.count();

        boolean catalogReady = books > 0 && authors > 0 && genres > 0;

        builder.up()
                .withDetail("books", books)
                .withDetail("authors", authors)
                .withDetail("genres", genres)
                .withDetail("catalogReady", catalogReady);

        if (!catalogReady) {
            builder.status(DEGRADED)
                    .withDetail("message", "Каталог доступен, но не заполнен (одна или несколько таблиц пустые)");
        }

    }
}