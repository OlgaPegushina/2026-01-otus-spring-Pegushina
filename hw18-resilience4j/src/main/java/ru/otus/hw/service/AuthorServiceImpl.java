package ru.otus.hw.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.repository.AuthorRepository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    private static final String CB_NAME = "authorService";

    private final AuthorRepository authorRepository;

    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "findAllFallback")
    @Retry(name = CB_NAME)
    public List<AuthorDto> findAll() {
        return authorMapper.toAuthorDtoList(authorRepository.findAll());
    }

    private List<AuthorDto> findAllFallback(Throwable ex) {
        log.warn("Деградация AuthorService.findAll(): список авторов временно недоступен. Возвращаю пустой список.");
        log.debug("Причина деградации AuthorService.findAll()", ex);
        return Collections.emptyList();
    }

}
