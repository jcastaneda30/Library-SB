package com.library.scraper.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "scraper_rule")
public class ScraperRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id")
    private ScraperSite site;

    // Orden de evaluación entre reglas del mismo campo
    @Column(nullable = false)
    private int position;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScraperField field;

    // Selector CSS si la página es HTML, JSON Pointer (p. ej. /data/title) si es JSON
    @Column(nullable = false)
    private String selector;
    // HTML: text (por defecto), html, ownText o un atributo (src, abs:href, ...). JSON: puntero relativo a cada elemento
    private String attribute;
    // Regex opcional; si tiene grupo se queda con el grupo 1
    private String pattern;
}
