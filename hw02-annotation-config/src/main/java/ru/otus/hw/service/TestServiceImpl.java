package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        if (questions.isEmpty()) {
            ioService.printLine("Questions not found!");
            return testResult;
        }

        printHeader();

        for (int i = 0; i < questions.size(); i++) {
            processQuestion(questions.get(i), i + 1, testResult);
        }
        return testResult;
    }

    private void printHeader() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
    }

    private void processQuestion(Question question, int index, TestResult testResult) {
        ioService.printFormattedLine("%n%d) %s", index, question.text());

        var answers = question.answers();

        if (answers == null || answers.isEmpty()) {
            ioService.printLine("Error: No answers found for this question.");
            return;
        }

        printAnswers(answers);

        int answerIndex = readValidAnswerIndex(answers);

        var isAnswerValid = answers.get(answerIndex - 1).isCorrect();

        testResult.applyAnswer(question, isAnswerValid);
    }

    private void printAnswers(List<Answer> answers) {
        for (int j = 0; j < answers.size(); j++) {
            ioService.printFormattedLine("  %d. %s", j + 1, answers.get(j).text());
        }
    }

    private int readValidAnswerIndex(List<Answer> answers) {
        int minValue = 1;
        int maxValue = answers.size();
        return ioService.readIntForRangeWithPrompt(minValue, maxValue, "Enter answer number:",
                String.format("Invalid input. Please enter a number between %d and %d", minValue, maxValue));
    }
}
