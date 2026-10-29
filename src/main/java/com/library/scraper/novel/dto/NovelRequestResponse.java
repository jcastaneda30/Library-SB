package com.library.scraper.novel.dto;

import com.library.scraper.novel.model.NovelRequest;
import com.library.scraper.novel.model.NovelRequestStatus;

import java.time.LocalDateTime;

public record NovelRequestResponse(
        Long id,
        Long siteId,
        Long bookId,
        String url,
        boolean includeChapters,
        Integer fromChapter,
        Integer toChapter,
        NovelRequestStatus status,
        Integer totalChapters,
        int processedChapters,
        String error,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
    public static NovelRequestResponse from(NovelRequest request) {
        return new NovelRequestResponse(
                request.getId(),
                request.getSite().getId(),
                request.getBook() == null ? null : request.getBook().getId(),
                request.getUrl(),
                request.isIncludeChapters(),
                request.getFromChapter(),
                request.getToChapter(),
                request.getStatus(),
                request.getTotalChapters(),
                request.getProcessedChapters(),
                request.getError(),
                request.getStartedAt(),
                request.getFinishedAt()
        );
    }
}
