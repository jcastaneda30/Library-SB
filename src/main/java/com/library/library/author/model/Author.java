package com.library.library.author.model;

import com.library.library.book.model.Book;
import com.library.common.model.Timer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "author")
public class Author extends Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alias;

    @ManyToMany(mappedBy = "authors")
    private Set<Book> books = new HashSet<>();
}
