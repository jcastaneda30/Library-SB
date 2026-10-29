package com.library.library.genre.controller;

import com.library.common.config.OpenApiConfig;
import com.library.library.genre.dto.CreateGenreRequest;
import com.library.library.genre.dto.GenreResponse;
import com.library.library.genre.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/genres")
@Tag(name = OpenApiConfig.GENRES)
public class GenreController {
    private final GenreService genreService;
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }
    @Operation(summary = "Listar géneros", description = "Todos los géneros con sus libros.")
    @GetMapping
    public List<GenreResponse> getAllGenres() {
        return genreService.findAllGenre();
    }

    @Operation(summary = "Obtener un género")
    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {
        return genreService.findGenreById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un género", description = "El nombre es único.")
    @PostMapping
    public ResponseEntity<GenreResponse> saveGenre(@Valid @RequestBody CreateGenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.saveGenre(request));
    }
}
