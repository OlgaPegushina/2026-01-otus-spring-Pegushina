package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.domain.jpa.GenreEntity;
import ru.otus.hw.domain.mongo.GenreDoc;

@Component
public class GenreMapperImpl implements GenreMapper {
    @Override
    public GenreDoc toDoc(GenreEntity genreEntity) {
        if (genreEntity == null) {
            return null;
        }

        return new GenreDoc(String.valueOf(genreEntity.getId()), genreEntity.getName());
    }
}
