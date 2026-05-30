package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.service.AuthorService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
@DisplayName("Тестирование контроллера для авторов")
class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthorService authorService;

    @Test
    @DisplayName("должен корректно отображать страницу со списком всех авторов")
    void shouldReturnAllAuthorsPage() throws Exception {
        List<AuthorDto> authors = List.of(
                new AuthorDto(1L, "Александр Пушкин"),
                new AuthorDto(2L, "Лев Толстой")
        );
        given(authorService.findAll()).willReturn(authors);

        mvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-authors"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(content().string(containsString("Александр Пушкин")))
                .andExpect(content().string(containsString("Лев Толстой")));
    }
}