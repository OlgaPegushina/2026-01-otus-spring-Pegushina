package ru.otus.hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.service.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest(CommentController.class)
@DisplayName("Тестирование контроллера комментариев (WebFlux)")
class CommentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Должен возвращать все комментарии для книги по ID")
    void shouldGetAllCommentsByBookId() {
        long bookId = 1L;
        List<CommentDto> comments = List.of(new CommentDto(1L, "Comment 1"),
                new CommentDto(2L, "Comment 2"));
        given(commentService.findAllByBookId(bookId)).willReturn(Flux.fromIterable(comments));

        webTestClient.get().uri("/comments/book/{bookId}", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CommentDto.class)
                .isEqualTo(comments);
    }

    @Test
    @DisplayName("Должен возвращать комментарий по ID")
    void shouldGetCommentById() {
        long commentId = 1L;
        CommentDto comment = new CommentDto(commentId, "Comment 1");
        given(commentService.findById(commentId)).willReturn(Mono.just(comment));

        webTestClient.get().uri("/comments/{commentId}", commentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommentDto.class)
                .isEqualTo(comment);
    }

    @Test
    @DisplayName("Должен создавать новый комментарий")
    void shouldCreateComment() throws Exception {
        CommentCreateDto createDto = new CommentCreateDto("New comment", 1L);
        CommentDto expectedComment = new CommentDto(1L, "New comment");
        given(commentService.create(any(CommentCreateDto.class))).willReturn(Mono.just(expectedComment));

        webTestClient.post().uri("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommentDto.class)
                .isEqualTo(expectedComment);
    }

    @Test
    @DisplayName("Должен обновлять существующий комментарий")
    void shouldUpdateComment() throws Exception {
        long commentId = 1L;
        CommentUpdateDto updateDto = new CommentUpdateDto(commentId, "Updated text");
        CommentDto expectedComment = new CommentDto(commentId, "Updated text");
        given(commentService.update(any(CommentUpdateDto.class))).willReturn(Mono.just(expectedComment));

        webTestClient.put().uri("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommentDto.class)
                .isEqualTo(expectedComment);
    }

    @Test
    @DisplayName("Должен удалять комментарий по ID")
    void shouldDeleteComment() {
        long commentId = 1L;
        given(commentService.deleteById(commentId)).willReturn(Mono.empty());

        webTestClient.delete().uri("/comments/{commentId}", commentId)
                .exchange()
                .expectStatus().isNoContent();
    }
}