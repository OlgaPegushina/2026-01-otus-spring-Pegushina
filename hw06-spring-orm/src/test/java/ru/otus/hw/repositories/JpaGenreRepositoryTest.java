package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository genreRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findAll(): должен возвращать все жанры из БД (data.sql)")
    void shouldReturnAllGenres() {
        // -- expected через EntityManager (без репозитория)
        var expected = List.of(
                em.find(Genre.class, 1L),
                em.find(Genre.class, 2L),
                em.find(Genre.class, 3L),
                em.find(Genre.class, 4L),
                em.find(Genre.class, 5L),
                em.find(Genre.class, 6L)
        );

        assertThat(expected).doesNotContainNull();

        em.clear();

        var actual = genreRepository.findAll();

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("findAllByIds(): должен возвращать жанры только по заданным ids")
    void shouldReturnGenresByIds() {
        var ids = Set.of(1L, 3L, 6L);

        var expected = List.of(
                em.find(Genre.class, 1L),
                em.find(Genre.class, 3L),
                em.find(Genre.class, 6L)
        );

        assertThat(expected).doesNotContainNull();

        em.clear();

        var actual = genreRepository.findAllByIds(ids);

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("findAllByIds(): должен возвращать пустой список, если ids пустой")
    void shouldReturnEmptyListIfIdsIsEmpty() {
        var actual = genreRepository.findAllByIds(Set.of());

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("findAllByIds(): должен игнорировать несуществующие ids и вернуть только существующие")
    void shouldReturnOnlyExistingGenresIfSomeIdsNotFound() {
        var ids = Set.of(2L, 999L);

        var expected = List.of(em.find(Genre.class, 2L));
        assertThat(expected).doesNotContainNull();

        em.clear();

        var actual = genreRepository.findAllByIds(ids);

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected);
    }
}
