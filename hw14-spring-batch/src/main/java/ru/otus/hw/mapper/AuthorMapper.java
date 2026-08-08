package ru.otus.hw.mapper;

import ru.otus.hw.domain.jpa.AuthorEntity;
import ru.otus.hw.domain.mongo.AuthorDoc;

public interface AuthorMapper {
    AuthorDoc toDoc(AuthorEntity e);
}
