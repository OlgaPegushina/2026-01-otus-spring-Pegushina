package ru.otus.hw.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.otus.hw.model.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, Long>, BookRepositoryCustom {

}
