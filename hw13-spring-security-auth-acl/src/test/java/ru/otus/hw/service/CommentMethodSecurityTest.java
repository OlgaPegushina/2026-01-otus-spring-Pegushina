package ru.otus.hw.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.otus.hw.config.CommentSecurity;
import ru.otus.hw.config.MethodSecurityConfig;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.model.Comment;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.CommentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Method security: @PreAuthorize в CommentServiceImpl")
@SpringJUnitConfig(classes = CommentMethodSecurityTest.Config.class)
class CommentMethodSecurityTest {

    private static final long COMMENT_ID = 1L;

    @Configuration
    @Import({MethodSecurityConfig.class, CommentServiceImpl.class})
    static class Config {
        @Bean
        CommentRepository commentRepository() {
            return mock(CommentRepository.class);
        }

        @Bean
        BookRepository bookRepository() {
            return mock(BookRepository.class);
        }

        @Bean
        CommentMapper commentMapper() {
            return mock(CommentMapper.class);
        }

        // --имя бина "commentSecurity", т.к. в @PreAuthorize стоит @commentSecurity
        @Bean(name = "commentSecurity")
        CommentSecurity commentSecurity(CommentRepository commentRepository) {
            return new CommentSecurity(commentRepository);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private CommentService commentService;

    @org.springframework.beans.factory.annotation.Autowired
    private CommentRepository commentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private CommentMapper commentMapper;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(commentRepository, commentMapper);
    }

    @Test
    @DisplayName("update: ADMIN может")
    void updateAdminAllowed() {
        asUser("admin", "ROLE_ADMIN");
        stubUpdateFlow(COMMENT_ID, "Updated text");

        assertDoesNotThrow(() ->
                commentService.update(new CommentUpdateDto(COMMENT_ID, "Updated text"))
        );
    }

    @Test
    @DisplayName("update: OWNER может")
    void updateOwnerAllowed() {
        asUser("owner", "ROLE_USER");
        when(commentRepository.existsByIdAndCreatedBy(eq(COMMENT_ID), eq("owner"))).thenReturn(true);
        stubUpdateFlow(COMMENT_ID, "Updated text");

        assertDoesNotThrow(() ->
                commentService.update(new CommentUpdateDto(COMMENT_ID, "Updated text"))
        );
    }

    @Test
    @DisplayName("update: не OWNER и не ADMIN - 403 (AccessDeniedException)")
    void updateNotOwnerDenied() {
        asUser("stranger", "ROLE_USER");
        when(commentRepository.existsByIdAndCreatedBy(eq(COMMENT_ID), eq("stranger"))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                commentService.update(new CommentUpdateDto(COMMENT_ID, "Updated text"))
        );
    }

    @Test
    @DisplayName("update: anonymous - 403 (AccessDeniedException)")
    void updateAnonymousDenied() {
        asAnonymous();

        assertThrows(AccessDeniedException.class, () ->
                commentService.update(new CommentUpdateDto(COMMENT_ID, "Updated text"))
        );
    }

    @Test
    @DisplayName("delete: ADMIN может")
    void deleteAdminAllowed() {
        asUser("admin", "ROLE_ADMIN");

        assertDoesNotThrow(() -> commentService.deleteById(COMMENT_ID));
        verify(commentRepository, times(1)).deleteById(eq(COMMENT_ID));
    }

    @Test
    @DisplayName("delete: OWNER может")
    void deleteOwnerAllowed() {
        asUser("owner", "ROLE_USER");
        when(commentRepository.existsByIdAndCreatedBy(eq(COMMENT_ID), eq("owner"))).thenReturn(true);

        assertDoesNotThrow(() -> commentService.deleteById(COMMENT_ID));
        verify(commentRepository, times(1)).deleteById(eq(COMMENT_ID));
    }

    @Test
    @DisplayName("delete: не OWNER и не ADMIN - 403 (AccessDeniedException)")
    void deleteNotOwnerDenied() {
        asUser("stranger", "ROLE_USER");
        when(commentRepository.existsByIdAndCreatedBy(eq(COMMENT_ID), eq("stranger"))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> commentService.deleteById(COMMENT_ID));
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete: anonymous - 403 (AccessDeniedException)")
    void deleteAnonymousDenied() {
        asAnonymous();

        assertThrows(AccessDeniedException.class, () -> commentService.deleteById(COMMENT_ID));
        verify(commentRepository, never()).deleteById(anyLong());
    }

    private void asUser(String username, String authority) {
        var auth = new UsernamePasswordAuthenticationToken(
                username,
                "N/A",
                AuthorityUtils.createAuthorityList(authority)
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void asAnonymous() {
        var auth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void stubUpdateFlow(long id, String newText) {
        var comment = mock(Comment.class);

        when(commentRepository.findById(eq(id))).thenReturn(Optional.of(comment));
        when(commentRepository.save(eq(comment))).thenReturn(comment);
        when(commentMapper.toCommentDto(eq(comment))).thenReturn(new CommentDto(id, newText));
    }
}