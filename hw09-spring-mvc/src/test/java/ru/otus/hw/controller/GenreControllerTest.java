package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.service.GenreService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenreController.class)
@DisplayName("Тестирование контроллера для жанров")
class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GenreService genreService;

    @Test
    @DisplayName("должен корректно отображать страницу со списком всех жанров")
    void shouldReturnAllGenresPage() throws Exception {
        List<GenreDto> genres = List.of(
                new GenreDto(1L, "Роман"),
                new GenreDto(2L, "Фантастика")
        );
        given(genreService.findAll()).willReturn(genres);

        mvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-genres"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("genres", genres))
                .andExpect(content().string(containsString("Роман")))
                .andExpect(content().string(containsString("Фантастика")));
    }
}