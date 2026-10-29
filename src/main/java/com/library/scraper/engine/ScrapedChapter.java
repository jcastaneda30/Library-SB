package com.library.scraper.engine;

import java.util.List;

public record ScrapedChapter(String url, String title, List<ExtractedParagraph> paragraphs) {}
