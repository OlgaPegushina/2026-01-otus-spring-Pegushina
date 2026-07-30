package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.otus.hw.repository.CommentRepository;

@Component("commentSecurity")
@RequiredArgsConstructor
public class CommentSecurity {
    private final CommentRepository commentRepository;

    public boolean isOwner(Long commentId, Authentication authentication) {
        if (commentId == null || authentication == null || authentication.getName() == null) {
            return false;
        }
        return commentRepository.existsByIdAndCreatedBy(commentId, authentication.getName());
    }
}
