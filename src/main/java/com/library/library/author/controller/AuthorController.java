package com.library.library.author.controller;

import com.library.common.config.OpenApiConfig;
import com.library.library.author.dto.AuthorResponse;
import com.library.library.author.dto.CreateAuthorRequest;
import com.library.library.author.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/authors")
@Tag(name = OpenApiConfig.AUTHORS)
public class AuthorController {
    private final AuthorService authorService;
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(summary = "Listar autores", description = "Todos los autores con sus libros.")
    @GetMapping
    public List<AuthorResponse> findAllAuthors() {
        return authorService.findAllAuthors();
    }

    @Operation(summary = "Obtener un autor")
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> findAuthorById(@PathVariable Long id){
        return authorService.findAuthorById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un autor")
    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody CreateAuthorRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.saveAuthor(request));
    }
}
