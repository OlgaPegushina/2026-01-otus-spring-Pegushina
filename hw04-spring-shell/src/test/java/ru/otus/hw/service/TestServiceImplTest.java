package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("Сервис тестирования (TestServiceImpl)")
@SpringBootTest(classes = TestServiceImpl.class)
public class TestServiceImplTest {
    @MockitoBean
    private QuestionDao questionDao;

    @MockitoBean
    private LocalizedIOService ioService;

    @Autowired
    private TestServiceImpl testService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("Petr", "Petrov");

        var question = new Question("Question", List.of(
                new Answer("Correct answer", true),
                new Answer("Wrong answer", false)
        ));

        given(questionDao.findAll()).willReturn(List.of(question));
    }

    @Test
    @DisplayName("должен засчитать правильный ответ")
    void shouldExecuteTestCountCorrectAnswer() {

        given(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(1);

        var result = testService.executeTestFor(student);

        assertEquals(1, result.getRightAnswersCount(), "Должен быть 1 правильный ответ");

        verify(questionDao).findAll();
    }

    @Test
    @DisplayName("НЕ должен засчитать неправильный ответ")
    void shouldExecuteTestNotCountIncorrectAnswer() {

        given(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(2);

        var result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount(), "Количество правильных ответов должно быть 0");
    }
}