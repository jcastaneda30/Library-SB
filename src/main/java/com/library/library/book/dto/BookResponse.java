package com.library.library.book.dto;

import com.library.library.author.dto.AuthorSummary;
import com.library.library.book.model.Book;
import com.library.library.book.model.NovelStatus;
import com.library.library.genre.dto.GenreSummary;
import com.library.library.image.controller.ImageUrls;


import java.time.LocalDateTime;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String description,
        Integer pages,
        LocalDateTime publishedAt,
        NovelStatus status,
        Integer totalChapters,
        String sourceUrl,
        String coverUrl,
        List<AuthorSummary> authors,
        List<GenreSummary> genres
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getPages(),
                book.getPublishedAt(),
                book.getStatus(),
                book.getTotalChapters(),
                book.getSourceUrl(),
                book.getCoverImage() == null ? null : ImageUrls.of(book.getCoverImage().getId()),
                book.getAuthors().stream().map(AuthorSummary::from).toList(),
                book.getGenres().stream().map(GenreSummary::from).toList()
        );
    }
}
