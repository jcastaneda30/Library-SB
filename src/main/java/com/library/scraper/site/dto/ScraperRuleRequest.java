package com.library.scraper.site.dto;

import com.library.scraper.site.model.ScraperField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScraperRuleRequest(
        @NotNull ScraperField field,
        @NotBlank String selector,
        String attribute,
        String pattern
) {}
