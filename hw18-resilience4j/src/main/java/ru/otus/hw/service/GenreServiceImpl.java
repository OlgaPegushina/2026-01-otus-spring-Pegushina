package ru.otus.hw.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.repository.GenreRepository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class GenreServiceImpl implements GenreService {
    private static final String CB_NAME = "genreService";

    private final GenreRepository genreRepository;

    private final GenreMapper genreMapper;

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "findAllFallback")
    @Retry(name = CB_NAME)
    public List<GenreDto> findAll() {

        return genreMapper.toGenreDtoList(genreRepository.findAll());
    }

    private List<GenreDto> findAllFallback(Throwable ex) {
        log.warn("Деградация GenreService.findAll(): список жанров временно недоступен. Возвращаю пустой список.");
        log.debug("Причина деградации GenreService.findAll()", ex);
        return Collections.emptyList();
    }
}
