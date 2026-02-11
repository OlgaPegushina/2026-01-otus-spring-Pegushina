package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        List<Question> questions = questionDao.findAll();

        if (questions.isEmpty()) {
            ioService.printLine("Questions not found!");
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            var question = questions.get(i);
            ioService.printFormattedLine("%n%d) %s", i + 1, question.text());
            var answers = question.answers();
            if (answers != null) {
                for (int j = 0; j < answers.size(); j++) {
                    var answer = answers.get(j);
                    ioService.printFormattedLine("  %d. %s", j + 1, answer.text());
                }
            }
        }
    }
}
