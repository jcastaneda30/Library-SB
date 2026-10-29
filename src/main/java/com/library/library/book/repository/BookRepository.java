package com.library.library.book.repository;

import com.library.library.book.model.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Carga autores y géneros en la misma consulta (evita N+1)
    @Override
    @EntityGraph(attributePaths = {"authors", "genres"})
    List<Book> findAll();

    @Override
    @EntityGraph(attributePaths = {"authors", "genres"})
    Optional<Book> findById(Long id);

    Optional<Book> findBySourceUrl(String sourceUrl);

    Optional<Book> findByTitle(String title);
}
