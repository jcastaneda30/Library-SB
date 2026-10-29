package com.library.library.chapter.dto;

import com.library.library.chapter.model.Chapter;

import java.util.List;

public record ChapterResponse(Long id, int number, String title, String sourceUrl, List<ParagraphResponse> paragraphs) {
    public static ChapterResponse from(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getNumber(),
                chapter.getTitle(),
                chapter.getSourceUrl(),
                chapter.getParagraphs().stream().map(ParagraphResponse::from).toList()
        );
    }
}
