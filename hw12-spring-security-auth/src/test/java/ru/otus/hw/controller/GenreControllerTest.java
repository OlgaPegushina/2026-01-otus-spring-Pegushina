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
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.service.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Контроллер для работы с жанрами")
@WebMvcTest(GenreController.class)
@Import(SecurityConfig.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenreService genreService;

    @Test
    @DisplayName("GET /genres — без аутентификации возвращает 401")
    void listGenresShouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/genres"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("должен корректно возвращать список всех жанров")
    @WithMockUser
    void shouldReturnCorrectGenresList() throws Exception {
        List<GenreDto> expectedGenres = List.of(
                new GenreDto(1L, "Genre 1"),
                new GenreDto(2L, "Genre 2")
        );

        given(genreService.findAll()).willReturn(expectedGenres);

        mvc.perform(get("/genres").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedGenres)));
    }
}