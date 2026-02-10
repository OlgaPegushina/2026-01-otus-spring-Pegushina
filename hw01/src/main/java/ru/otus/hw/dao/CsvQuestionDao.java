package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();
        java.net.URL resource = getClass().getClassLoader().getResource(fileName);

        if (resource == null) {
            throw new QuestionReadException("File not found: " + fileName);
        }

        try (InputStream inputStream = resource.openStream()) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            List<QuestionDto> questionDtos = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withType(QuestionDto.class).withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true).withSkipLines(1).build().parse();
            if (questionDtos.isEmpty()) {
                throw new QuestionReadException("File is empty (no questions found)");
            }
            return questionDtos.stream()
                    .map(QuestionDto::toDomainObject)
                    .toList();
        } catch (QuestionReadException qre) {
            throw qre;
        } catch (Exception e) {
            throw new QuestionReadException("Error reading questions from CSV", e);
        }
    }
}