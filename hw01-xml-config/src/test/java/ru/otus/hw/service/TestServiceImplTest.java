package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TestServiceImplTest {
    @Mock
    private QuestionDao questionDao;

    @Mock
    private IOService ioService;

    @InjectMocks
    private TestServiceImpl testService;

    @Test
    @DisplayName("должен вызывать DAO для получения вопросов")
    void shouldCallDaoToGetQuestions() {
        Question question = new Question("What command shows the commit history in Git?", List.of(
                new Answer("git log", true),
                new Answer("git history", false)
        ));
        given(questionDao.findAll()).willReturn(List.of(question));

        testService.executeTest();

        verify(questionDao).findAll();
    }
}
