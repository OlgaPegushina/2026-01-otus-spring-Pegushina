package ru.otus.hw.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.service.AuthorService;
import ru.otus.hw.service.BookService;
import ru.otus.hw.service.CommentService;
import ru.otus.hw.service.GenreService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@DisplayName("Тестирование контроллера для книг")
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CommentService commentService;

    private List<AuthorDto> authors;
    private List<GenreDto> genres;
    private BookDto sampleBookDto;
    private List<CommentDto> sampleComments;

    @BeforeEach
    void setUp() {
        authors = List.of(new AuthorDto(1L, "Александр Пушкин"));
        genres = List.of(new GenreDto(1L, "Роман"), new GenreDto(2L, "Поэма"));
        sampleBookDto = new BookDto(1L, "Капитанская дочка", authors.get(0), List.of(genres.get(0)));
        sampleComments = List.of(
                new CommentDto(101L, "Отличная книга!"),
                new CommentDto(102L, "Читал в школе, понравилось.")
        );
    }

    @Test
    @DisplayName("должен отображать страницу со списком всех книг")
    void shouldReturnAllBooksPage() throws Exception {
        given(bookService.findAll()).willReturn(List.of(sampleBookDto));

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(content().string(containsString("Капитанская дочка")));
    }

    @Test
    @DisplayName("должен отображать страницу создания новой книги со списками авторов и жанров")
    void shouldReturnAddBookPage() throws Exception {
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);

        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-form"))
                .andExpect(model().attributeExists("authors", "genres"))
                .andExpect(content().string(containsString("Александр Пушкин")))
                .andExpect(content().string(containsString("Роман")));
    }

    @Test
    @DisplayName("должен успешно создавать новую книгу и делать редирект")
    void shouldCreateBookAndRedirect() throws Exception {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("title", "Новая книга");
        params.add("authorId", "1");
        params.add("genreIds", "1");
        params.add("genreIds", "2");

        mvc.perform(post("/books/new").params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService, times(1)).create(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("должен успешно обновлять книгу")
    void shouldUpdateBookSuccessfully() throws Exception {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("id", "1");
        params.add("title", "Обновленная книга");
        params.add("authorId", "1");
        params.add("genreIds", "1");

        mvc.perform(post("/books/edit/1").params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService, times(1)).update(
                new BookUpdateDto(1L, "Обновленная книга", 1L, List.of(1L))
        );
    }

    @Test
    @DisplayName("должен удалять книгу и делать редирект")
    void shouldDeleteBookAndRedirect() throws Exception {
        mvc.perform(post("/books/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("должен отображать страницу с деталями книги и ее комментариями")
    void shouldReturnBookDetailsPageWithComments() throws Exception {
        long bookId = sampleBookDto.id();
        // Настраиваем моки для обоих сервисов
        given(bookService.findById(bookId)).willReturn(sampleBookDto);
        given(commentService.findAllByBookId(bookId)).willReturn(sampleComments);

        mvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("book", sampleBookDto))
                .andExpect(model().attribute("comments", sampleComments))
                .andExpect(content().string(containsString("Капитанская дочка")))
                .andExpect(content().string(containsString("Отличная книга!"))); // Проверяем наличие текста комментария
    }

    @Test
    @DisplayName("должен успешно создавать новый комментарий и делать редирект")
    void shouldCreateCommentAndRedirect() throws Exception {
        long bookId = 1L;

        mvc.perform(post("/books/{bookId}/comments", bookId)
                        .param("text", "Новый комментарий")
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId));

        verify(commentService, times(1)).create(any());
    }

    @Test
    @DisplayName("должен возвращать страницу деталей с ошибкой валидации при создании пустого комментария")
    void shouldReturnDetailsPageWithErrorsOnEmptyComment() throws Exception {
        long bookId = 1L;
        given(bookService.findById(bookId)).willReturn(sampleBookDto);
        given(commentService.findAllByBookId(bookId)).willReturn(sampleComments);

        mvc.perform(post("/books/{bookId}/comments", bookId)
                        .param("text", "")
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().hasErrors());

        verify(commentService, never()).create(any());
    }

    @Test
    @DisplayName("должен удалять комментарий и делать редирект на страницу книги")
    void shouldDeleteCommentAndRedirect() throws Exception {
        long bookId = 1L;
        long commentId = 101L;

        mvc.perform(post("/books/{bookId}/comments/{commentId}/delete", bookId, commentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId));

        verify(commentService, times(1)).deleteById(commentId);
    }

    @Test
    @DisplayName("должен отображать страницу редактирования комментария")
    void shouldReturnEditCommentPage() throws Exception {
        long bookId = 1L;
        long commentId = 101L;
        var commentDto = new CommentDto(commentId, "Текст для редактирования");

        given(commentService.findById(commentId)).willReturn(Optional.of(commentDto));

        mvc.perform(get("/books/{bookId}/comments/{commentId}/edit", bookId, commentId))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-edit"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(content().string(containsString("Текст для редактирования")));
    }

    @Test
    @DisplayName("должен возвращать 404 Not Found, если книга для редактирования не найдена")
    void shouldReturn404WhenBookForEditNotFound() throws Exception {
        long nonExistentId = 99L;
        given(bookService.findForUpdate(nonExistentId))
                .willThrow(new EntityNotFoundException("Книга с id=" + nonExistentId + " не найдена"));

        mvc.perform(get("/books/edit/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(containsString("Книга с id=" + nonExistentId + " не найдена")));
    }
}