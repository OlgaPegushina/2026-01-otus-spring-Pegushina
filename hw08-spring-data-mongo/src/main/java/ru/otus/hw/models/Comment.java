package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    private String text;

    @DBRef(lazy = true)
    @ToString.Exclude
    private Book book;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
