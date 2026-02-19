package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@DisplayName("Сервис тестирования")
@ExtendWith(MockitoExtension.class)
public class TestServiceImplTest {
    @Mock
    private QuestionDao questionDao;

    @Mock
    private IOService ioService;

    @InjectMocks
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

        given(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(1);

        var result = testService.executeTestFor(student);

        assertEquals(1, result.getRightAnswersCount(), "Должен быть 1 правильный ответ");

        verify(questionDao).findAll();
    }

    @Test
    @DisplayName("НЕ должен засчитать неправильный ответ")
    void shouldExecuteTestNotCountIncorrectAnswer() {

        given(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(2);

        var result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount(), "Количество правильных ответов должно быть 0");
    }
}