package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.model.Book;
import ru.otus.hw.model.Comment;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.CommentRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    @Override
    public CommentDto findById(long id) {
        return commentMapper.toCommentDto(commentRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id))));

    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> findAllByBookId(long bookId) {
        findBookOrThrow(bookId);
        return commentMapper.toCommentDtoList(commentRepository.findAllByBookId(bookId));
    }

    @Transactional
    @Override
    public CommentDto create(CommentCreateDto commentDto) {
        var book = findBookOrThrow(commentDto.bookId());

        var comment = new Comment(0, commentDto.text(), book);

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto update(CommentUpdateDto commentDto) {
        Comment commentToUpdate = commentRepository.findById(commentDto.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id %d not found".formatted(commentDto.id())));

        commentToUpdate.setText(commentDto.text());

        return commentMapper.toCommentDto(commentRepository.save(commentToUpdate));
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        commentRepository.deleteById(id);
    }

    private Book findBookOrThrow(long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId)));
    }
}
