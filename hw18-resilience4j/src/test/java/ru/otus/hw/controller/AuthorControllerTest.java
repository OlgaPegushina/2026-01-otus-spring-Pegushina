package ru.otus.hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.service.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Контроллер для работы с авторами")
@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mvc; // виртуальный браузер для отправки запросов

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService authorService;

    @Test
    @DisplayName("должен корректно возвращать список всех авторов")
    void shouldReturnCorrectAuthorsList() throws Exception {
        List<AuthorDto> expectedAuthors = List.of(
                new AuthorDto(1L, "Author 1"),
                new AuthorDto(2L, "Author 2")
        );

        given(authorService.findAll()).willReturn(expectedAuthors);

        mvc.perform(get("/authors").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedAuthors)));
    }
}