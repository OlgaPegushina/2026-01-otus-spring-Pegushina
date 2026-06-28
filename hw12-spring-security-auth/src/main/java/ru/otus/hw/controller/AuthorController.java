package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.service.AuthorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping("/authors")
    @ResponseStatus(HttpStatus.OK)
    public List<AuthorDto> getAllAuthors() {
        return authorService.findAll();
    }
}
