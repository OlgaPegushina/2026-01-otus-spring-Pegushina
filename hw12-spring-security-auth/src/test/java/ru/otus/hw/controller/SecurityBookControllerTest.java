package ru.otus.hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.service.BookService;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Контроллер c проверкой безопасности для работы с книгами")
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
public class SecurityBookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("GET /books — без аутентификации возвращает 401")
    void listBooksShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /books/{id} — без аутентификации возвращает 401")
    void viewBookShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/books/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /books — без аутентификации возвращает 401")
    void createBookShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BookCreateDto("Title", 1L, List.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /books — без аутентификации возвращает 401")
    void updateBookShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(put("/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BookUpdateDto(1L, "Title", 1L, List.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /books/{id} — без аутентификации возвращает 401")
    void deleteBookShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(delete("/books/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
