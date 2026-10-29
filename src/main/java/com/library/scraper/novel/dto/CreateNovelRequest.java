package com.library.scraper.novel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

// url: página de la novela, p. ej. https://novelbin.xyz/novel/skyfire-avenue. Sin rango se traen todos los capítulos que falten
public record CreateNovelRequest(
        @NotBlank String url,
        boolean includeChapters,
        @Positive Integer fromChapter,
        @Positive Integer toChapter
) {}
