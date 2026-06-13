package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.*;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.exception.handler.ErrorHandler;
import ru.otus.hw.service.BookService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest(BookController.class)
@Import(ErrorHandler.class)
@DisplayName("Тестирование контроллера книг")
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    private static final AuthorDto AUTHOR_1 = new AuthorDto(1L, "Author 1");
    private static final AuthorDto AUTHOR_2 = new AuthorDto(2L, "Author 2");

    private static final GenreDto GENRE_1 = new GenreDto(1L, "Genre 1");
    private static final GenreDto GENRE_2 = new GenreDto(2L, "Genre 2");
    private static final GenreDto GENRE_3 = new GenreDto(3L, "Genre 3");
    private static final GenreDto GENRE_4 = new GenreDto(4L, "Genre 4");

    private static final BookDto BOOK_1 = new BookDto(1L, "Book 1", AUTHOR_1, List.of(GENRE_1, GENRE_2));
    private static final BookDto BOOK_2 = new BookDto(2L, "Book 2", AUTHOR_2, List.of(GENRE_3, GENRE_4));


    @Test
    @DisplayName("Должен возвращать список всех книг")
    void shouldReturnAllBooks() {
        List<BookDto> allBooks = List.of(BOOK_1, BOOK_2);
        given(bookService.findAll()).willReturn(Flux.fromIterable(allBooks));

        webTestClient.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookDto.class)
                .isEqualTo(allBooks);
    }

    @Test
    @DisplayName("Должен возвращать книгу по ID")
    void shouldReturnBookById() {
        given(bookService.findById(BOOK_1.id())).willReturn(Mono.just(BOOK_1));

        webTestClient.get().uri("/books/{bookId}", BOOK_1.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .isEqualTo(BOOK_1);
    }

    @Test
    @DisplayName("Должен создавать новую книгу")
    void shouldCreateBook() {
        var bookToCreate = new BookCreateDto("Book 3", AUTHOR_2.id(), List.of(GENRE_3.id(), GENRE_4.id()));
        var expectedCreatedBook = new BookDto(3L, "Book 3", AUTHOR_2, List.of(GENRE_3, GENRE_4));

        given(bookService.create(any(BookCreateDto.class))).willReturn(Mono.just(expectedCreatedBook));

        webTestClient.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookDto.class)
                .isEqualTo(expectedCreatedBook);
    }

    @Test
    @DisplayName("Должен обновлять существующую книгу")
    void shouldUpdateBook() {
        var bookToUpdate = new BookUpdateDto(BOOK_1.id(), "Book 1 Updated", AUTHOR_1.id(), List.of(GENRE_1.id()));
        var expectedUpdatedBook = new BookDto(BOOK_1.id(), "Book 1 Updated", AUTHOR_1, List.of(GENRE_1));

        given(bookService.update(any(BookUpdateDto.class))).willReturn(Mono.just(expectedUpdatedBook));

        webTestClient.put().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookToUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .isEqualTo(expectedUpdatedBook);
    }

    @Test
    @DisplayName("Должен удалять книгу по ID")
    void shouldDeleteBook() {
        given(bookService.deleteById(BOOK_1.id())).willReturn(Mono.empty());

        webTestClient.delete().uri("/books/{bookId}", BOOK_1.id())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Должен возвращать 404 Not Found, если книга не найдена")
    void shouldReturnNotFoundWhenBookDoesNotExist() {
        long nonExistentId = 999L;
        String errorMessage = "Book with id " + nonExistentId + " not found";
        given(bookService.findById(nonExistentId)).willReturn(Mono.error(new EntityNotFoundException(errorMessage)));

        webTestClient.get().uri("/books/{bookId}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("Должен возвращать 400 Bad Request при создании книги с невалидными данными")
    void shouldReturnBadRequestWhenCreatingBookWithInvalidData() {
        var invalidBookToCreate = new BookCreateDto("", AUTHOR_1.id(), List.of(GENRE_1.id()));

        webTestClient.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBookToCreate)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.reason").isEqualTo("Ошибка валидации");
    }

    @Test
    @DisplayName("Должен возвращать 500 Internal Server Error при неожиданной ошибке в сервисе")
    void shouldReturnInternalServerErrorOnUnexpectedException() {
        long bookId = 1L;
        String errorMessage = "Что-то пошло не так!";
        given(bookService.findById(bookId)).willReturn(Mono.error(new RuntimeException(errorMessage)));

        webTestClient.get().uri("/books/{bookId}", bookId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.reason").isEqualTo("Ошибка сервера")
                .jsonPath("$.message").isEqualTo(errorMessage);
    }
}