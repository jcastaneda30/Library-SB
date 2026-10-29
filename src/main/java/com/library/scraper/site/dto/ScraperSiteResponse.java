package com.library.scraper.site.dto;

import com.library.scraper.site.model.ScraperSite;

import java.util.List;

public record ScraperSiteResponse(
        Long id,
        String name,
        String baseUrl,
        boolean enabled,
        String userAgent,
        int requestDelayMs,
        String chapterListUrlTemplate,
        String chapterUrlTemplate,
        List<ScraperRuleResponse> rules
) {
    public static ScraperSiteResponse from(ScraperSite site) {
        return new ScraperSiteResponse(
                site.getId(),
                site.getName(),
                site.getBaseUrl(),
                site.isEnabled(),
                site.getUserAgent(),
                site.getRequestDelayMs(),
                site.getChapterListUrlTemplate(),
                site.getChapterUrlTemplate(),
                site.getRules().stream().map(ScraperRuleResponse::from).toList()
        );
    }
}
