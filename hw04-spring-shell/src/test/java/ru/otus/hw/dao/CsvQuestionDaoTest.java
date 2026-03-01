package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.LocalizedIOService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@DisplayName("загрузка вопросов и ответов")
@SpringBootTest(classes = CsvQuestionDao.class)
public class CsvQuestionDaoTest {
    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @MockitoBean
    private LocalizedIOService ioService;

    @Autowired
    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
        // -- lenient(), чтобы тесты без ошибок не ругались на неиспользованный мок.
        lenient().when(ioService.getMessage(anyString(), any())).thenReturn("Localized error message");
        lenient().when(ioService.getMessage(anyString())).thenReturn("Localized error message");
    }

    @Test
    @DisplayName("должен найти все вопросы")
    void shouldFindAllQuestion() {
        given(fileNameProvider.getTestFileName()).willReturn("questions-test.csv");

        List<Question> questions = csvQuestionDao.findAll();

        assertEquals(2, questions.size(), "должно быть 2 вопроса");

        Question q = questions.get(1);

        assertEquals("What is the purpose of the final keyword in Java?", q.text(), "текст вопроса неверный");
        assertEquals(3, q.answers().size(), "должно быть 3 ответа");
        assertTrue(q.answers().getFirst().isCorrect(), "верным должен быть первый ответ");
    }

    @Test
    @DisplayName("проверить пустой файл (должен выбросить исключение когда парсится файл")
    void shouldThrowExceptionWhenParsingFails() {
        given(fileNameProvider.getTestFileName()).willReturn("questions-empty.csv");

        assertThrows(QuestionReadException.class, () -> csvQuestionDao.findAll());
    }

    @Test
    @DisplayName("должен выбросить исключение, когда не нашел файла")
    void shouldThrowsExceptionWhenFileNotFound() {

        String fileName = "not-found";

        given(fileNameProvider.getTestFileName()).willReturn(fileName);

        given(ioService.getMessage(anyString(), any())).willReturn("File not found: " + fileName);

        QuestionReadException qre = assertThrows(QuestionReadException.class,
                () -> csvQuestionDao.findAll());

        assertEquals("File not found: " + fileName, qre.getMessage(),
                "сообщение об ошибке не совпадает");
    }
}
