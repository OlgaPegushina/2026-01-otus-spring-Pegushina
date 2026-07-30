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
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.service.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Контроллер c проверкой безопасности для работы с комментариями")
@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
public class SecurityCommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("GET /comments/book/{id} — без аутентификации возвращает 401")
    void getCommentsForBookShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/comments/book/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /comments/{id} — без аутентификации возвращает 401")
    void getCommentByIdShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/comments/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /comments — без аутентификации возвращает 401")
    void addCommentShouldReturn401WhenNotAuthenticated() throws Exception {
        var dto = new CommentCreateDto("Brand new comment", 1L);

        mvc.perform(post("/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /comments — без аутентификации возвращает 401")
    void updateCommentShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(put("/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentUpdateDto(1L, "Updated text"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /comments/{id} — без аутентификации возвращает 401")
    void deleteCommentShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(delete("/comments/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /comments — с аутентификацией, но без CSRF возвращает 403")
    @WithMockUser(username = "user")
    void addCommentShouldReturn403WhenNoCsrf() throws Exception {
        var dto = new CommentCreateDto("Brand new comment", 1L);

        mvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /comments — с аутентификацией, но без CSRF возвращает 403")
    @WithMockUser
    void updateCommentShouldReturn403WhenNoCsrf() throws Exception {
        var dto = new CommentUpdateDto(1L, "Updated text");

        mvc.perform(put("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /comments/{id} — с аутентификацией, но без CSRF возвращает 403")
    @WithMockUser
    void deleteCommentShouldReturn403WhenNoCsrf() throws Exception {
        mvc.perform(delete("/comments/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("должен возвращать все комментарии для книги")
    @WithMockUser
    void shouldGetAllCommentsForBook() throws Exception {
        long bookId = 1L;

        List<CommentDto> comments = List.of(
                new CommentDto(1L, "Comment text 1"),
                new CommentDto(2L, "Comment text 2")
        );

        given(commentService.findAllByBookId(eq(bookId))).willReturn(comments);

        mvc.perform(get("/comments/book/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(comments)));
    }

    @Test
    @DisplayName("должен возвращать комментарий по ID")
    @WithMockUser
    void shouldGetCommentById() throws Exception {
        long commentId = 1L;

        var expectedComment = new CommentDto(commentId, "A specific comment");
        given(commentService.findById(eq(commentId))).willReturn(expectedComment);

        mvc.perform(get("/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedComment)));
    }

    @Test
    @DisplayName("должен корректно создавать новый комментарий и передавать username")
    @WithMockUser(username = "user")
    void shouldCreateComment() throws Exception {
        long bookId = 1L;

        var createDto = new CommentCreateDto("Brand new comment", bookId);
        var expectedComment = new CommentDto(1L, "Brand new comment");

        given(commentService.create(any(CommentCreateDto.class), eq("user")))
                .willReturn(expectedComment);

        mvc.perform(post("/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedComment)));

        verify(commentService, times(1)).create(any(CommentCreateDto.class), eq("user"));
    }

    @Test
    @DisplayName("должен корректно обновлять комментарий")
    @WithMockUser
    void shouldUpdateComment() throws Exception {
        long commentId = 1L;

        var updateDto = new CommentUpdateDto(commentId, "Updated text");
        var expectedComment = new CommentDto(commentId, "Updated text");

        given(commentService.update(any(CommentUpdateDto.class))).willReturn(expectedComment);

        mvc.perform(put("/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedComment)));

        verify(commentService, times(1)).update(any(CommentUpdateDto.class));
    }

    @Test
    @DisplayName("должен корректно удалять комментарий по ID")
    @WithMockUser
    void shouldDeleteComment() throws Exception {
        long commentId = 1L;

        mvc.perform(delete("/comments/{commentId}", commentId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteById(eq(commentId));
    }
}