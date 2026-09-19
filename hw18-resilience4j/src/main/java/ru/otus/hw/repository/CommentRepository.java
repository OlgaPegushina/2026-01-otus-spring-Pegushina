package ru.otus.hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.model.Comment;

import java.util.List;

@RepositoryRestResource(path = "library-comments")
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByBookId(Long bookId);
}
