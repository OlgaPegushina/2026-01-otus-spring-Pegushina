package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@DisplayName("загрузка вопросов и ответов")
@ExtendWith(MockitoExtension.class)
public class CsvQuestionDaoTest {
    @Mock
    private TestFileNameProvider fileNameProvider;

    @InjectMocks
    private CsvQuestionDao csvQuestionDao;

    @Test
    @DisplayName("должен найти все вопросы")
    void shouldFindAllQuestion() {
        given(fileNameProvider.getTestFileName()).willReturn("questions-test.csv");

        List<Question> questions = csvQuestionDao.findAll();

        Question q = questions.get(1);

        assertEquals(2, questions.size(), "должно быть 2 вопроса");
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
        given(fileNameProvider.getTestFileName()).willReturn("not-found");

        QuestionReadException qre = assertThrows(QuestionReadException.class,
                () -> csvQuestionDao.findAll());

        assertEquals("File not found: not-found", qre.getMessage(),
                "сообщение должно быть: \"File not found: not-found\"");
    }
}
