package ru.otus.hw.processor; // или туда, где у тебя лежат процессоры

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.jpa.AuthorEntity;
import ru.otus.hw.domain.jpa.BookEntity;
import ru.otus.hw.domain.mongo.BookDoc;
import ru.otus.hw.mapper.BookMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookProcessor implements ItemProcessor<BookEntity, BookDoc> {

    private final EntityManager em;

    private final BookMapper bookMapper;

    @Override
    public BookDoc process(BookEntity book) {
        AuthorEntity author = em.find(AuthorEntity.class, book.getAuthor().getId());

        List<Long> genreIds = em.createQuery(
                        "SELECT g.id FROM BookEntity b JOIN b.genres g WHERE b.id = :bookId",
                        Long.class)
                .setParameter("bookId", book.getId())
                .getResultList();

        return bookMapper.toDoc(book, author, genreIds);
    }
}

