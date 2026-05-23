package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(GenreServiceImpl.class)
class GenreServiceImplTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Genre.class);
    }

    @Test
    @DisplayName("должен возвращать все жанры из базы данных")
    void findAll_shouldReturnAllGenres() {
        var expectedGenres = genreRepository.saveAll(List.of(
                new Genre("id1", "Genre_1"),
                new Genre("id2", "Genre_2")
        ));

        var actualGenres = genreService.findAll();

        assertThat(actualGenres)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedGenres);
    }

    @Test
    @DisplayName("должен возвращать пустой список, если жанров нет")
    void findAll_shouldReturnEmptyList_whenNoGenresExist() {
        var genres = genreService.findAll();

        assertThat(genres).isEmpty();
    }
}
