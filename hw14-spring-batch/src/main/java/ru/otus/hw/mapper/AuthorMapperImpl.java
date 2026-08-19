package ru.otus.hw.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.domain.jpa.AuthorEntity;
import ru.otus.hw.domain.mongo.AuthorDoc;

@Component
public class AuthorMapperImpl implements AuthorMapper {
    @Override
    public AuthorDoc toDoc(AuthorEntity authorEntity) {
        if (authorEntity == null) {
            return null;
        }

        return new AuthorDoc(String.valueOf(authorEntity.getId()), authorEntity.getFullName());
    }
}
