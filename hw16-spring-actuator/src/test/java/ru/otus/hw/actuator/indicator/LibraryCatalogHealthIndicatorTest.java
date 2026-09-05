package ru.otus.hw.actuator.indicator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.GenreRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Тестирование кастомного HealthIndicator библиотеки: проверка состояния каталога")
@ExtendWith(MockitoExtension.class)
class LibraryCatalogHealthIndicatorTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private LibraryCatalogHealthIndicator indicator;

    @DisplayName("Возвращает статус UP, если каталог полностью заполнен")
    @Test
    void shouldReturnUpWhenCatalogIsReady() {
        when(bookRepository.count()).thenReturn(3L);
        when(authorRepository.count()).thenReturn(2L);
        when(genreRepository.count()).thenReturn(5L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("books", 3L)
                .containsEntry("authors", 2L)
                .containsEntry("genres", 5L)
                .containsEntry("catalogReady", true);

        assertThat(health.getDetails()).doesNotContainKey("message");
    }

    @DisplayName("Возвращает статус DEGRADED, если хотя бы одна из таблиц пустая")
    @Test
    void shouldReturnDegradedWhenAnyTableIsEmpty() {
        when(bookRepository.count()).thenReturn(0L);
        when(authorRepository.count()).thenReturn(2L);
        when(genreRepository.count()).thenReturn(5L);

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DEGRADED");
        assertThat(health.getDetails())
                .containsEntry("books", 0L)
                .containsEntry("authors", 2L)
                .containsEntry("genres", 5L)
                .containsEntry("catalogReady", false)
                .containsEntry("message",
                        "Каталог доступен, но не заполнен (одна или несколько таблиц пустые)");
    }

    @DisplayName("Возвращает статус DOWN, если репозиторий выбрасывает исключение")
    @Test
    void shouldReturnDownWhenRepositoryThrowsException() {
        when(bookRepository.count()).thenThrow(new RuntimeException("DB is down"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);

        assertThat(health.getDetails()).containsKey("error");
    }
}
