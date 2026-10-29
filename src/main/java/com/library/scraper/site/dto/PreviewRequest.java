package com.library.scraper.site.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PreviewRequest(
        @Schema(description = "Página de una novela bajo la URL base del sitio",
                example = "https://novelbin.xyz/novel/skyfire-avenue")
        @NotBlank String url
) {}
