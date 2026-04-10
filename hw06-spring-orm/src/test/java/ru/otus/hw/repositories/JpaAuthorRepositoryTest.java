package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuthorRepository.class)
public class JpaAuthorRepositoryTest {

    @Autowired
    private JpaAuthorRepository authorRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findById(): должен возвращать автора по id")
    void shouldFindAuthorById() {
        var expected = em.find(Author.class, 1L);
        assertThat(expected).isNotNull();

        em.clear();

        var actual = authorRepository.findById(1L);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("findById(): должен возвращать empty для несуществующего id")
    void shouldReturnEmptyIfAuthorNotFound() {
        em.clear();

        var actual = authorRepository.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("findAll(): должен возвращать всех авторов из БД (data.sql)")
    void shouldReturnAllAuthors() {
        // -- expected через EntityManager (без репозитория)
        var expected = List.of(
                em.find(Author.class, 1L),
                em.find(Author.class, 2L),
                em.find(Author.class, 3L)
        );

        assertThat(expected).allMatch(Objects::nonNull);

        em.clear();

        var actual = authorRepository.findAll();

        // -- сравнение "по полям", без equals()
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

    }
  }
