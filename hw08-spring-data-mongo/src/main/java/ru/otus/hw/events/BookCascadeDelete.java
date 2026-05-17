package ru.otus.hw.events;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.CommentRepository;

@Component
@RequiredArgsConstructor
public class BookCascadeDelete extends AbstractMongoEventListener<Book> {

    private final CommentRepository commentRepository;

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Book> event) {
        Object id = event.getSource().get("_id");
        if (id != null) {
            String bookId = id.toString();
            commentRepository.deleteAllByBookId(bookId);
        }
    }
}
