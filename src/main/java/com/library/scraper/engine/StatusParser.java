package com.library.scraper.engine;

import com.library.library.book.model.NovelStatus;

import java.util.regex.Pattern;

// Traduce el texto libre que muestra cada sitio ("Completed", "En curso", ...) al estado de la biblioteca
public final class StatusParser {

    private static final Pattern COMPLETED_TEXT = Pattern.compile("complet|finish|\\bended\\b|conclu|terminad|finalizad");
    private static final Pattern HIATUS_TEXT = Pattern.compile("hiatus|paus|dropped|abandon");
    private static final Pattern ONGOING_TEXT = Pattern.compile("ongoing|on-going|serializ|updating|en curso|emisi|publicando");

    private StatusParser() {
    }

    public static NovelStatus parse(String text) {
        if (text == null || text.isBlank()) {
            return NovelStatus.UNKNOWN;
        }
        String normalized = text.toLowerCase();
        if (HIATUS_TEXT.matcher(normalized).find()) return NovelStatus.HIATUS;
        if (COMPLETED_TEXT.matcher(normalized).find()) return NovelStatus.COMPLETED;
        if (ONGOING_TEXT.matcher(normalized).find()) return NovelStatus.ONGOING;
        return NovelStatus.UNKNOWN;
    }
}
