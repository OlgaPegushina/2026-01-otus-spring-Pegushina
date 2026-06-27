package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.service.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;

@WebFluxTest(GenreController.class)
@DisplayName("Тестирование контроллера жанров (WebFlux)")
class GenreControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GenreService genreService;

    @Test
    @DisplayName("Должен возвращать Flux всех жанров")
    void shouldReturnAllGenres() {
        List<GenreDto> genresList = List.of(
                new GenreDto(1L, "Genre 1"),
                new GenreDto(2L, "Genre 2")
        );
        Flux<GenreDto> genresFlux = Flux.fromIterable(genresList);

        given(genreService.findAll()).willReturn(genresFlux);

        webTestClient.get().uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GenreDto.class)
                .hasSize(2)
                .isEqualTo(genresList);
    }
}