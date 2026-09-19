package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.model.Comment;

import java.util.Collections;
import java.util.List;

@Component
public class CommentMapperImpl implements CommentMapper {
    @Override
    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDto(comment.getId(), comment.getText());
    }

    @Override
    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }

        return comments.stream()
                .map(this::toCommentDto)
                .toList();
    }
}
