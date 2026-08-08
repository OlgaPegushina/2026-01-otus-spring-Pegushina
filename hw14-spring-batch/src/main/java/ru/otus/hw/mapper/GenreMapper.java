package ru.otus.hw.mapper;

import ru.otus.hw.domain.jpa.GenreEntity;
import ru.otus.hw.domain.mongo.GenreDoc;

public interface GenreMapper {
    GenreDoc toDoc(GenreEntity e);
}
