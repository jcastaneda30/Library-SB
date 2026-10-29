package com.library.library.book.controller;

import com.library.common.config.OpenApiConfig;
import com.library.library.book.dto.BookResponse;
import com.library.library.book.dto.CreateBookRequest;
import com.library.library.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/books")
@Tag(name = OpenApiConfig.BOOKS)
public class BookController {
    private final BookService bookService;
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    @Operation(summary = "Listar libros")
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.findAllBooks();
    }

    @Operation(summary = "Obtener un libro", description = "Los libros importados traen estado, total de capítulos, URL de origen y `coverUrl` absoluta; `pages` y `publishedAt` son `null`.")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return bookService.findBookById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un libro a mano", description = "`authorIds` y `genreIds` deben existir; si no, responde `400`.")
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.saveBook(request));
    }
}
