package ru.otus.hw.domain.mongo;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "genres")
public class GenreDoc {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenreDoc genre = (GenreDoc) o;
        return Objects.equals(id, genre.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
