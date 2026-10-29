package com.library.library.chapter.model;

import com.library.library.book.model.Book;
import com.library.common.model.Timer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "chapter")
public class Chapter extends Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    // Posición del capítulo dentro del libro, empezando en 1
    @Column(nullable = false)
    private int number;
    private String title;
    private String sourceUrl;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<ChapterParagraph> paragraphs = new ArrayList<>();

    public void addParagraph(ChapterParagraph paragraph) {
        paragraph.setChapter(this);
        paragraph.setPosition(paragraphs.size());
        paragraphs.add(paragraph);
    }
}
