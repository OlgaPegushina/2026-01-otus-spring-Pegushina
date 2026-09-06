package ru.otus.hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.model.Author;

@RepositoryRestResource(path = "library-authors")
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
