package com.library.library.genre.controller;

import com.library.library.genre.dto.CreateGenreRequest;
import com.library.library.genre.dto.GenreResponse;
import com.library.library.genre.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/genres")
public class GenreController {
    private final GenreService genreService;
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }
    @GetMapping
    public List<GenreResponse> getAllGenres() {
        return genreService.findAllGenre();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {
        return genreService.findGenreById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GenreResponse> saveGenre(@Valid @RequestBody CreateGenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.saveGenre(request));
    }
}
