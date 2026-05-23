package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

    @Override
    Optional<Book> findById(String id);

    @Override
    List<Book> findAll();
}
