package ru.otus.hw.service;

import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;

import java.util.List;

public interface CommentService {
    CommentDto findById(long id);

    List<CommentDto> findAllByBookId(long bookId);

    CommentDto create(CommentCreateDto commentDto);

    CommentDto update(CommentUpdateDto commentDto);

    void deleteById(long id);
}
