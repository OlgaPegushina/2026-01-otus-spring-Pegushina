package ru.otus.hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.service.BookService;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Контроллер для работы с книгами")
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("должен возвращать корректный список книг")
    @WithMockUser
    void shouldReturnCorrectBooksList() throws Exception {
        var author = new AuthorDto(1L, "Author");
        var genre = new GenreDto(1L, "Genre");
        List<BookDto> books = List.of(
                new BookDto(1L, "Book 1", author, List.of(genre)),
                new BookDto(2L, "Book 2", author, List.of(genre))
        );
        given(bookService.findAll()).willReturn(books);

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(books)));
    }

    @Test
    @DisplayName("должен корректно создавать новую книгу")
    @WithMockUser
    void shouldCreateBook() throws Exception {
        var author = new AuthorDto(1L, "Author");
        var genre = new GenreDto(1L, "Genre");
        var createDto = new BookCreateDto("New Book", 1L, List.of(1L));
        var expectedBook = new BookDto(1L, "New Book", author, List.of(genre));

        given(bookService.create(any(BookCreateDto.class))).willReturn(expectedBook);

        mvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedBook)));

        verify(bookService, times(1)).create(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("должен корректно обновлять книгу")
    @WithMockUser
    void shouldUpdateBook() throws Exception {
        var author = new AuthorDto(1L, "Author");
        var genre = new GenreDto(1L, "Genre");
        var updateDto = new BookUpdateDto(1L,"Updated Book", 1L, List.of(1L));
        var expectedBook = new BookDto(1L, "Updated Book", author, List.of(genre));

        given(bookService.update(any(BookUpdateDto.class))).willReturn(expectedBook);

        mvc.perform(put("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedBook)));

        verify(bookService, times(1)).update(any(BookUpdateDto.class));
    }

    @Test
    @DisplayName("должен корректно удалять книгу по ID")
    @WithMockUser
    void shouldDeleteBook() throws Exception {
        long bookId = 1L;

        mvc.perform(delete("/books/{bookId}", bookId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteById(eq(bookId));
    }

    @Test
    @DisplayName("должен возвращать книгу по ID")
    @WithMockUser
    void shouldReturnBookById() throws Exception {
        long bookId = 1L;
        var author = new AuthorDto(1L, "Author");
        var genre = new GenreDto(1L, "Genre");
        var expectedBook = new BookDto(bookId, "The Book", author, List.of(genre));

        given(bookService.findById(eq(bookId))).willReturn(expectedBook);

        mvc.perform(get("/books/{bookId}", bookId))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedBook)));
    }

    @Test
    @DisplayName("должен возвращать статус 404, если книга не найдена")
    @WithMockUser
    void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        long nonExistentId = 999L;
        String errorMessage = "Book with id " + nonExistentId + " not found";
        given(bookService.findById(anyLong())).willThrow(new EntityNotFoundException(errorMessage));

        mvc.perform(get("/books/{bookId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("Запрашиваемый ресурс не найден"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("должен возвращать статус 400 при ошибке валидации (создание книги)")
    @WithMockUser
    void shouldReturnBadRequestOnValidationFailure() throws Exception {
        var invalidCreateDto = new BookCreateDto("", 1L, List.of(1L));

        mvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message", allOf(
                        containsString("Название книги не может быть пустым"),
                        containsString("Название должно содержать от 2 до 255 символов")
                )));
    }

    @Test
    @DisplayName("должен возвращать статус 500 при непредвиденной ошибке сервера")
    @WithMockUser
    void shouldReturnInternalServerErrorOnUnexpectedError() throws Exception {
        String errorMessage = "Something went terribly wrong";
        given(bookService.findAll()).willThrow(new RuntimeException(errorMessage));

        mvc.perform(get("/books"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.reason").value("Ошибка сервера"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}