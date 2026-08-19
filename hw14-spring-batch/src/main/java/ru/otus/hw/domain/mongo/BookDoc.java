package ru.otus.hw.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class BookDoc {
    @Id
    private String id;

    private String title;

    private String authorId;

    private List<String> genreIds;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookDoc book = (BookDoc) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
