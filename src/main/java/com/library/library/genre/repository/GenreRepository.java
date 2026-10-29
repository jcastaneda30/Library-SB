package com.library.library.genre.repository;

import com.library.library.genre.model.Genre;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Override
    @EntityGraph(attributePaths = {"books"})
    List<Genre> findAll();

    @Override
    @EntityGraph(attributePaths = {"books"})
    Optional<Genre> findById(Long id);

    Optional<Genre> findByNameIgnoreCase(String name);
}
