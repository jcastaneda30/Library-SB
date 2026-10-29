package com.library.scraper.site.dto;

import jakarta.validation.constraints.NotBlank;

public record PreviewRequest(@NotBlank String url) {}
