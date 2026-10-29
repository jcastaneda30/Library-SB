package com.library.library.chapter.dto;

import java.util.List;

public record ImportChapterRequest(
        Long bookId,
        int number,
        String title,
        String sourceUrl,
        List<ImportParagraph> paragraphs
) {}
