package com.library.scraper.engine;

import java.util.List;

public record ScrapedNovel(
        String title,
        String coverUrl,
        List<String> authors,
        String synopsis,
        List<String> genres,
        String status,
        Integer chapterCount,
        String novelId
) {}
