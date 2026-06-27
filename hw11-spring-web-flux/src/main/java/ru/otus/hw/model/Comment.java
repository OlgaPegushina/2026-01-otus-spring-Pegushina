package ru.otus.hw.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @Setter(lombok.AccessLevel.NONE)
    private long id;

    private String text;

    @Column("book_id")
    private long bookId;

    @Transient
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
        Comment other = (Comment) o;
        return id != 0 && id == other.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
