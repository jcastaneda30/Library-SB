package com.library.library.model;

import com.library.common.model.Timer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name="book")
public class Book extends Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private int pages;
    @Column(nullable = false)
    private LocalDateTime publishedAt;

    @ManyToMany
    @JoinTable(
            name = "book_author", // 1. Nombre de la tabla intermedia que se va a crear
            joinColumns = @JoinColumn(name = "book_id"), // 2. Columna que apunta a esta clase (Book)
            inverseJoinColumns = @JoinColumn(name = "author_id") // 3. Columna que apunta a la otra clase (Author)
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "book_genre", // 1. Nombre de la tabla intermedia que se va a crear
            joinColumns = @JoinColumn(name = "book_id"), // 2. Columna que apunta a esta clase (Book)
            inverseJoinColumns = @JoinColumn(name = "genre_id") // 3. Columna que apunta a la otra clase (Author)
    )
    private Set<Genre> genres = new HashSet<>();
}
