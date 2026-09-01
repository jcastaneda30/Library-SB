package com.library.library.controller;

import com.library.library.model.Genre;
import com.library.library.repository.GenreRepository;
import com.library.library.service.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/genres")
public class GenreController {
    private final GenreService genreRepository;
    public GenreController(GenreService genreRepository) {
        this.genreRepository = genreRepository;
    }
    @GetMapping
    public List<Genre> getAllGenres() {
        return genreRepository.findAllGenre();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable Long id) {
        Optional<Genre> genre = genreRepository.findGenreById(id);
        return genre.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Genre> saveGenre(@RequestBody Genre genre) {
        return new ResponseEntity<>(genreRepository.saveGenre(genre), HttpStatus.CREATED);
    }
}
