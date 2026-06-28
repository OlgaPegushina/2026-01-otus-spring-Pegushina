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
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.service.CommentService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Контроллер для работы с комментариями")
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
    @DisplayName("POST /comments — без аутентификации возвращает 401")
    void addCommentShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/comments")
                        .with(csrf())
                        .param("bookId", "1")
                        .param("text", "Brand new comment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /books — без аутентификации возвращает 401")
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
}
