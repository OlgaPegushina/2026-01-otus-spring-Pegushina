package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.LocalizedIOService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class CsvQuestionDao implements QuestionDao {

    private final TestFileNameProvider fileNameProvider;

    private final LocalizedIOService ioService;

    @Override
    public List<Question> findAll() {

        String fileName = fileNameProvider.getTestFileName();
        java.net.URL resource = getClass().getClassLoader().getResource(fileName);

        if (resource == null) {
            throw new QuestionReadException(ioService.getMessage(
                    "QuestionReadException.error.file.not.found", fileName));
        }

        return readQuestions(resource);
    }

    private List<Question> readQuestions(java.net.URL resource) {

        try (InputStream inputStream = resource.openStream()) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            List<QuestionDto> questionDtos = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withType(QuestionDto.class).withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true).withSkipLines(1).build().parse();

            if (questionDtos.isEmpty()) {
                throw new QuestionReadException(ioService.getMessage(
                        "QuestionReadException.error.file.empty"));
            }

            return questionDtos.stream()
                    .map(QuestionDto::toDomainObject)
                    .toList();

        } catch (QuestionReadException qre) {
            throw qre;
        } catch (Exception e) {
            throw new QuestionReadException(ioService.getMessage("QuestionReadException.error.reading"));
        }
    }
}