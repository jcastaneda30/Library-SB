package com.library.library.chapter.repository;

import com.library.library.chapter.model.Chapter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByBookIdOrderByNumber(Long bookId);

    // Carga los párrafos en la misma consulta (evita N+1)
    @EntityGraph(attributePaths = {"paragraphs"})
    Optional<Chapter> findByBookIdAndNumber(Long bookId, int number);

    @Query("select c.number from Chapter c where c.book.id = :bookId")
    List<Integer> findNumbersByBookId(@Param("bookId") Long bookId);
}
