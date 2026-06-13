package ru.otus.hw.mapper;

import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.model.Comment;

public interface CommentMapper {
    CommentDto toCommentDto(Comment comment);
}
