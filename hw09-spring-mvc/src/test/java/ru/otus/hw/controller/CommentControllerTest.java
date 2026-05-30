package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.service.BookService;
import ru.otus.hw.service.CommentService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@DisplayName("Тестирование контроллера для комментариев")
class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("должен корректно отображать страницу со всеми комментариями для конкретной книги")
    void shouldReturnCommentsPageForBook() throws Exception {
        long bookId = 1L;

        var author = new AuthorDto(1L, "Тестовый Автор");
        var genre = new GenreDto(1L, "Тестовый Жанр");
        var book = new BookDto(bookId, "Книга для комментариев", author, List.of(genre));
        var comments = List.of(
                new CommentDto(101L, "Это первый комментарий"),
                new CommentDto(102L, "Это второй комментарий")
        );

        given(bookService.findById(bookId)).willReturn(book);
        given(commentService.findAllByBookId(bookId)).willReturn(comments);


        mvc.perform(get("/comments/book/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("all-comments-book"))
                .andExpect(model().attributeExists("book", "comments"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("comments", comments))
                .andExpect(content().string(containsString("Книга для комментариев")))
                .andExpect(content().string(containsString("Это первый комментарий")));
    }
}