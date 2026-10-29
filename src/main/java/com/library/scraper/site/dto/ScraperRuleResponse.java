package com.library.scraper.site.dto;

import com.library.scraper.site.model.ScraperField;
import com.library.scraper.site.model.ScraperRule;

public record ScraperRuleResponse(Long id, ScraperField field, String selector, String attribute, String pattern) {
    public static ScraperRuleResponse from(ScraperRule rule) {
        return new ScraperRuleResponse(rule.getId(), rule.getField(), rule.getSelector(), rule.getAttribute(), rule.getPattern());
    }
}
