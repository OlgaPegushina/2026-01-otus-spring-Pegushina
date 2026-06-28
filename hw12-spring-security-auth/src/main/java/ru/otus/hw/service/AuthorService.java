package ru.otus.hw.service;

import ru.otus.hw.dto.AuthorDto;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> findAll();
}
