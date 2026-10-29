package com.library.scraper.site.dto;

import com.library.scraper.site.model.JavascriptRendering;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record SaveScraperSiteRequest(
        @Schema(example = "NovelBin")
        @NotBlank String name,
        @Schema(description = "Las novelas que se pidan deben estar bajo esta URL", example = "https://novelbin.xyz/")
        @NotBlank String baseUrl,
        @Schema(description = "Si es `false` no acepta peticiones de novelas", defaultValue = "true")
        Boolean enabled,
        @Schema(description = "User-Agent de las peticiones; por defecto `LibraryScraper/0.1`")
        String userAgent,
        @Schema(description = "Pausa mínima entre peticiones al mismo host", defaultValue = "1000", example = "1500")
        @PositiveOrZero Integer requestDelayMs,
        @Schema(description = "Qué páginas se abren en un navegador sin interfaz porque cargan con JavaScript",
                defaultValue = "NONE", example = "CHAPTERS")
        JavascriptRendering javascriptRendering,
        @Schema(description = "Con renderizado: elemento que debe aparecer antes de leer la página",
                example = ".reader-content p")
        String waitForSelector,
        @Schema(description = "URL del índice de capítulos si no está en la página de la novela. "
                + "Variables: `{url}`, `{origin}`, `{slug}`, `{id}`")
        String chapterListUrlTemplate,
        @Schema(description = "URL de cada capítulo. Sin reglas `CHAPTER_LINK` se genera de 1 a `CHAPTER_COUNT` con "
                + "`{n}` (desde 1) o `{i}` (desde 0); con ellas, cada valor se inserta como `{chapter}`",
                example = "{origin}/read/{slug}/chapter-{n}")
        String chapterUrlTemplate,
        @Schema(description = "Reglas de extracción; si hay varias para el mismo campo gana la primera que encuentre algo")
        @NotEmpty List<@Valid ScraperRuleRequest> rules
) {}
