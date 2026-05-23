package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(AuthorServiceImpl.class)
class AuthorServiceImplTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Author.class);
    }

    @Test
    @DisplayName("должен возвращать всех авторов из базы данных")
    void findAllShouldReturnAllAuthors() {
        var expectedAuthors = authorRepository.saveAll(List.of(
                new Author("id1", "Author_1"),
                new Author("id2", "Author_2")
        ));

        var actualAuthors = authorService.findAll();

        assertThat(actualAuthors)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedAuthors);
    }

    @Test
    @DisplayName("должен возвращать пустой список, если авторов нет")
    void findAll_shouldReturnEmptyList_whenNoAuthorsExist() {
        var authors = authorService.findAll();

        assertThat(authors).isEmpty();
    }
}
