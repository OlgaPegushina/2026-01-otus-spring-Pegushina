package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<Genre> findAll() {
        TypedQuery<Genre> genres = em.createQuery("select g from Genre g", Genre.class);
        return genres.getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        TypedQuery<Genre> genres = em.createQuery("select g from Genre g where g.id in (:ids)", Genre.class);
        genres.setParameter("ids", ids);
        return genres.getResultList();
    }
}
