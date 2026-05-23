package ru.otus.hw.mongock.changelog;

import com.mongodb.client.MongoDatabase;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import java.util.List;

@ChangeUnit(id = "init-database-data", order = "001", author = "olga")
public class DatabaseChangelog {

    @Execution
    public void seedDatabase(MongoTemplate template, MongoDatabase db) {
        db.drop();

        Author author1 = template.save(new Author("author1", "Author_1"));
        Author author2 = template.save(new Author("author2", "Author_2"));
        Author author3 = template.save(new Author("author3", "Author_3"));

        Genre genre1 = template.save(new Genre("genre1", "Genre_1"));
        Genre genre2 = template.save(new Genre("genre2", "Genre_2"));
        Genre genre3 = template.save(new Genre("genre3", "Genre_3"));

        Book book1 = template.save(new Book("book1", "BookTitle_1", author2, List.of(genre2)));
        Book book2 = template.save(new Book("book2", "BookTitle_2", author3, List.of(genre3)));
        Book book3 = template.save(new Book("book3", "BookTitle_3", author1, List.of(genre1)));

        template.insertAll(List.of(
                new Comment("comment1", "Comment_1", book1),
                new Comment("comment2", "Comment_2", book2),
                new Comment("comment3", "Comment_3", book3)
        ));
    }

    @RollbackExecution
    public void rollback(MongoDatabase db) {
        db.drop();
    }
}
