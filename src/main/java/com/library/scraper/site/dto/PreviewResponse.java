package com.library.scraper.site.dto;

import com.library.scraper.engine.ScrapedChapter;
import com.library.scraper.engine.ScrapedNovel;

import java.util.List;

// Resultado de probar una configuración sin guardar nada; los errores de capítulos no ocultan los datos de la novela
public record PreviewResponse(
        ScrapedNovel novel,
        int chaptersFound,
        List<String> firstChapterUrls,
        ScrapedChapter firstChapter,
        String chapterError
) {}
