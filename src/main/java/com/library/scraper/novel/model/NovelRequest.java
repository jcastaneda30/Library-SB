package com.library.scraper.novel.model;

import com.library.library.book.model.Book;
import com.library.common.model.Timer;
import com.library.scraper.site.model.ScraperSite;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Petición para extraer una novela concreta de un sitio configurado. Hay una sola por URL: pedir la
 * misma novela otra vez la vuelve a procesar (p. ej. para traer capítulos nuevos) en lugar de duplicarla.
 */
@Entity
@Getter
@Setter
@Table(name = "novel_request")
public class NovelRequest extends Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sitio bajo cuya URL base está la novela; de él salen las reglas de extracción
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id")
    private ScraperSite site;
    // Libro en el que se guardó la novela; se llena tras extraer la ficha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false, unique = true)
    private String url;

    // Parámetros de la última vez que se pidió
    @Column(nullable = false)
    private boolean includeChapters;
    private Integer fromChapter;
    private Integer toChapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NovelRequestStatus status = NovelRequestStatus.PENDING;
    private Integer totalChapters;
    @Column(nullable = false)
    private int processedChapters;
    private String error;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
