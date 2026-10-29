package com.library.scraper.site.model;

import com.library.common.model.Timer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de scraping de un sitio. Las plantillas de URL aceptan las variables
 * {url}, {origin}, {slug}, {id}, {n} (capítulo desde 1), {i} (capítulo desde 0) y {chapter}.
 */
@Entity
@Getter
@Setter
@Table(name = "scraper_site")
public class ScraperSite extends Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    // Las novelas que se pidan deben estar bajo esta URL, p. ej. https://novelbin.xyz/
    @Column(nullable = false, unique = true)
    private String baseUrl;
    @Column(nullable = false)
    private boolean enabled = true;
    private String userAgent;
    // Pausa mínima entre peticiones al mismo host
    @Column(nullable = false)
    private int requestDelayMs = 1000;

    // Los datos de la novela se leen de la URL pedida; estas plantillas solo ubican los capítulos.
    // URL del índice de capítulos; por defecto la página de la novela
    private String chapterListUrlTemplate;
    // Construye la URL de cada capítulo a partir de {n}/{i} o del valor de CHAPTER_LINK ({chapter})
    private String chapterUrlTemplate;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<ScraperRule> rules = new ArrayList<>();

    public void replaceRules(List<ScraperRule> newRules) {
        rules.clear();
        for (ScraperRule rule : newRules) {
            rule.setSite(this);
            rule.setPosition(rules.size());
            rules.add(rule);
        }
    }
}
