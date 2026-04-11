package ru.otus.hw.repositories;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph("book-author-genres")
    @NonNull
    Optional<Book> findById(@NonNull Long id);

    @Override
    @EntityGraph("book-author")
    @NonNull
    List<Book> findAll();
}
