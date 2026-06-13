package ru.otus.hw.mapper;

import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.model.Comment;

import java.util.List;

public interface CommentMapper {
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDtoList(List<Comment> comments);
}
