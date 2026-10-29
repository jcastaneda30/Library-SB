package com.library.scraper.novel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

// Sin rango se traen todos los capítulos que falten
public record CreateNovelRequest(
        @Schema(description = "Página de la novela, bajo la URL base de un sitio configurado",
                example = "https://novelbin.xyz/novel/skyfire-avenue")
        @NotBlank String url,
        @Schema(description = "Si es `false` solo se extrae la ficha", example = "true")
        boolean includeChapters,
        @Schema(description = "Primer capítulo a traer (desde 1)", example = "1")
        @Positive Integer fromChapter,
        @Schema(description = "Último capítulo a traer", example = "3")
        @Positive Integer toChapter
) {}
