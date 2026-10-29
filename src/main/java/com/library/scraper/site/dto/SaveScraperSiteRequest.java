package com.library.scraper.site.dto;

import com.library.scraper.site.model.JavascriptRendering;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record SaveScraperSiteRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        Boolean enabled,
        String userAgent,
        @PositiveOrZero Integer requestDelayMs,
        JavascriptRendering javascriptRendering,
        String waitForSelector,
        String chapterListUrlTemplate,
        String chapterUrlTemplate,
        @NotEmpty List<@Valid ScraperRuleRequest> rules
) {}
