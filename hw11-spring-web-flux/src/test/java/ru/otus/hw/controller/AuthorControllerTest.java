package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.service.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;

@WebFluxTest(AuthorController.class)
@DisplayName("Тестирование контроллера авторов (WebFlux)")
class AuthorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @Test
    @DisplayName("Должен возвращать Flux всех авторов")
    void shouldReturnAllAuthors() {
        List<AuthorDto> authorsList = List.of(
                new AuthorDto(1L, "Author 1"),
                new AuthorDto(2L, "Author 2")
        );
        Flux<AuthorDto> authorsFlux = Flux.fromIterable(authorsList);

        given(authorService.findAll()).willReturn(authorsFlux);

        webTestClient.get().uri("/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuthorDto.class)
                .hasSize(2)
                .isEqualTo(authorsList);
    }
}