package ru.otus.hw.domain.mongo;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "authors")
public class AuthorDoc {
    @Id
    private String id;

    private String fullName;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorDoc author = (AuthorDoc) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
