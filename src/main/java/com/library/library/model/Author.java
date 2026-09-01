package com.library.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.library.common.model.Timer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @JsonIgnoreProperties({"authors", "genres"})
    private Set<Book> books;
}
