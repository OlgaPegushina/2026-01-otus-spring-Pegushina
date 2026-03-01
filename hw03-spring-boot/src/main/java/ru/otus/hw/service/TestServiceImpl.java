package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.NumberAttemptsException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        printHeader();

        for (int i = 0; i < questions.size(); i++) {
            processQuestion(questions.get(i), i + 1, testResult);
        }
        return testResult;
    }

    private void printHeader() {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");
    }

    private void processQuestion(Question question, int index, TestResult testResult) {
        ioService.printFormattedLineLocalized("TestService.print.questions", index, question.text());

        var answers = question.answers();

        if (answers == null || answers.isEmpty()) {
            ioService.printLineLocalized("TestService.no.answers");
            return;
        }

        printAnswers(answers);

        int answerIndex = readValidAnswerIndex(answers);

        if (answerIndex == -1) {
            return;
        }

        var isAnswerValid = answers.get(answerIndex - 1).isCorrect();

        testResult.applyAnswer(question, isAnswerValid);
    }

    private void printAnswers(List<Answer> answers) {
        for (int j = 0; j < answers.size(); j++) {
            ioService.printFormattedLine("  %d. %s", j + 1, answers.get(j).text());
        }
    }

    private int readValidAnswerIndex(List<Answer> answers) {
        try {
            int minValue = 1;
            int maxValue = answers.size();
            return ioService.readIntForRangeWithPromptLocalized(minValue, maxValue, "TestService.enter.answer",
                    "TestService.invalid.input");
        } catch (NumberAttemptsException e) {
            ioService.printLineLocalized("StreamsIOService.error");
            return -1;
        }
    }
}
