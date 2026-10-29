package com.library.library.chapter.dto;

import com.library.library.chapter.model.Chapter;

public record ChapterSummary(Long id, int number, String title) {
    public static ChapterSummary from(Chapter chapter) {
        return new ChapterSummary(chapter.getId(), chapter.getNumber(), chapter.getTitle());
    }
}
