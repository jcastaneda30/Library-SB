package com.library.library.chapter.model;

import com.library.library.image.model.Image;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "chapter_paragraph")
public class ChapterParagraph {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Column(nullable = false)
    private int position;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParagraphType type;

    // TEXT: HTML saneado con solo etiquetas de formato (negrita, cursiva, ...)
    private String content;

    // IMAGE: imagen descargada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;
}
