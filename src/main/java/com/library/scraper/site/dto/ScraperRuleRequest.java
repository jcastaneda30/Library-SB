package com.library.scraper.site.dto;

import com.library.scraper.site.model.ScraperField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScraperRuleRequest(
        @Schema(description = "Dato que extrae la regla", example = "TITLE")
        @NotNull ScraperField field,
        @Schema(description = "Selector CSS si la página es HTML; JSON Pointer (`/data/title`) si es JSON",
                example = "#ssr-novel-card h1")
        @NotBlank String selector,
        @Schema(description = "HTML: `text` (por defecto), `html`, `ownText` o un atributo (`abs:src`, `abs:href`). "
                + "JSON: puntero relativo a cada elemento (`/name`)")
        String attribute,
        @Schema(description = "Regex opcional; si tiene un grupo se queda con el grupo 1", example = "novel-id: (\\d+)")
        String pattern
) {}
